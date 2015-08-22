package asset.pipeline.cdn

import com.bertramlabs.plugins.karman.CloudFile
import com.bertramlabs.plugins.karman.Directory
import com.bertramlabs.plugins.karman.StorageProvider
import com.bertramlabs.plugins.karman.util.Mimetypes
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

class AssetUpload extends DefaultTask {

    AssetPipelineCdnPluginConfig config
    Date expirationDate
    String gzip = 'true'
    StorageProvider localProvider
    Directory localDirectory
    List<AssetProviderConfig> providers = []

    void init() {
        config = project.assetsCdn

        localProvider = StorageProvider.create(
                provider: 'local',
                basePath: project.buildDir
        )
        localDirectory = localProvider[config.localStoragePath]

        providers = config.providers ?: []
        if (!providers) {
            // Single provider
            if (config.validate()) {
                config.provider = config.provider.toLowerCase()
                providers << config
            }
        } else {
            // Multiple providers
            providers = providers.collect { // Cast and validate provider config
                AssetProviderConfig providerConfig = new AssetProviderConfig(it)
                if (providerConfig.validate()) {
                    providerConfig.provider = providerConfig.provider?.toLowerCase()
                    if (!providerConfig.storagePath) {
                        providerConfig.storagePath = config.storagePath // Default storage path
                    }
                }
                providerConfig
            }
        }

        if (config.expires) {
            if (config.expires instanceof Date) {
                expirationDate = config.expires
            } else if (config.expires instanceof Integer) {
                expirationDate = new Date() + config.expires
            } else if (config.expires instanceof String && config.expires.isNumber()) {
                expirationDate = new Date() + config.expires.toInteger()
            }
        }

        gzip = config.gzip.toString()
    }

    @TaskAction
    def upload() {
        if (!localProvider) {
            init()
        }

        if (!localDirectory.exists()) {
            logger.error "Could not synchronize assets, ${localDirectory} local directory not found, please run assetCompile task first."
            return false
        }
        providers.eachWithIndex { providerConfig, index ->
            logger.lifecycle "Syncing assets with ${providerConfig.provider} storage provider (${index+1} of ${providers.size()})"
            synchronizeProvider(providerConfig.toMap())
        }
    }

    boolean synchronizeProvider(Map providerConfig) {
        try {
            String remoteDirectoryName = providerConfig.directory
            String remoteStoragePath = providerConfig.storagePath
            providerConfig.remove('expires')
            if (!remoteStoragePath.endsWith('/')) {
                remoteStoragePath = "${remoteStoragePath}/"
            }
            if (remoteStoragePath.startsWith('/')) {
                remoteStoragePath = remoteStoragePath.replaceFirst('/', '')
            }

            StorageProvider remoteProvider = StorageProvider.create(providerConfig)
            Directory remoteDirectory = remoteProvider[remoteDirectoryName]

            Map manifestFiles = [:]
            CloudFile localManifestFile = localDirectory['manifest.properties']
            if (localManifestFile.exists()) {
                CloudFile remoteManifestFile = remoteDirectory[remoteStoragePath + 'manifest.properties'] //Lets check if a remote manifest exists
                Properties remoteManifest = new Properties()
                if (remoteManifestFile.exists()) {
                    remoteManifest.load(remoteManifestFile.inputStream)
                }

                int count = 0
                localManifestFile.text.eachLine { line ->
                    String originalFileName = line.tokenize('=').first()
                    String compiledFileName = line.tokenize('=').last()
                    // Ignore file already defined in remote manifest
                    if (!line.startsWith('#')
                            && (!remoteManifest
                                || !remoteManifest.getProperty(originalFileName)
                                || remoteManifest.getProperty(originalFileName) != compiledFileName)) {
                        manifestFiles[originalFileName] = compiledFileName
                        CloudFile localFile = localDirectory[compiledFileName]

                        if (localFile.exists()) {
                            logger.lifecycle "Uploading file ${count+1} - ${localFile.name}"
                            CloudFile cloudFile = remoteDirectory[remoteStoragePath + localFile.name]
                            String cacheControl = "PUBLIC, max-age=${(expirationDate.time / 1000).toInteger()}, must-revalidate"

                            if (expirationDate) {
                                cloudFile.setMetaAttribute('Cache-Control', cacheControl)
                                cloudFile.setMetaAttribute('Expires', expirationDate)
                            }

                            CloudFile compressedLocalFile = localDirectory["${compiledFileName}.gz"]
                            if (gzip == 'true' && compressedLocalFile.exists()) {
                                // Upload compressed version
                                cloudFile.setMetaAttribute('Content-Encoding', 'gzip')
                                cloudFile.bytes = compressedLocalFile.bytes
                            } else {
                                // Upload original version
                                cloudFile.bytes = localFile.bytes
                            }

                            cloudFile.contentType = Mimetypes.instance.getMimetype(localFile.name)
                            cloudFile.save()
                            count++

                            if (gzip == 'both' && compressedLocalFile.exists()) {
                                // Upload additional compressed version (with .gz extension)
                                logger.lifecycle "Uploading File ${count+1} - ${compressedLocalFile.name}"
                                CloudFile compressedCloudFile = remoteDirectory[remoteStoragePath + compressedLocalFile.name]
                                compressedCloudFile.setMetaAttribute('Content-Encoding', 'gzip')

                                if (expirationDate) {
                                    compressedCloudFile.setMetaAttribute('Cache-Control', cacheControl)
                                    compressedCloudFile.setMetaAttribute('Expires', expirationDate)
                                }

                                compressedCloudFile.contentType = cloudFile.contentType
                                compressedCloudFile.bytes = compressedLocalFile.bytes
                                compressedCloudFile.save()
                                count++
                            }

                            String extension = originalFileName.tokenize('.').last()
                            if (extension in ['eot', 'svg', 'ttf', 'woff']) {
                                // Workaround for webfonts referenced in CSS, upload original file
                                logger.lifecycle "Uploading File ${count+1} - ${originalFileName}"
                                CloudFile originalCloudFile = remoteDirectory[remoteStoragePath + originalFileName]

                                if (expirationDate) {
                                    originalCloudFile.setMetaAttribute('Cache-Control', cacheControl)
                                    originalCloudFile.setMetaAttribute('Expires', expirationDate)
                                }

                                if (gzip == 'true' && compressedLocalFile.exists()) {
                                    // Upload compressed version
                                    originalCloudFile.setMetaAttribute('Content-Encoding', 'gzip')
                                    originalCloudFile.bytes = compressedLocalFile.bytes
                                } else {
                                    // Upload original version
                                    originalCloudFile.bytes = localFile.bytes
                                }

                                originalCloudFile.contentType = cloudFile.contentType
                                originalCloudFile.save()
                                count++
                            }
                        }
                    }
                }
                if (!count) {
                    logger.lifecycle "Syncing done - All the assets are already uploaded"
                } else {
                    logger.lifecycle "Syncing done - ${count} assets uploaded"
                }
                // Upload manifest
                remoteManifestFile.bytes = localManifestFile.bytes
                remoteManifestFile.save()
            }
        } catch(Exception e) {
            throw new TaskExecutionException(this, e)
        }
        true
    }

}
