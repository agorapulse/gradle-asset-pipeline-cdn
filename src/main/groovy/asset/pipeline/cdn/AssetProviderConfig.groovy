package asset.pipeline.cdn

import com.amazonaws.services.s3.AmazonS3Client
import com.bertramlabs.plugins.karman.CloudFileACL
import org.gradle.api.InvalidUserDataException

class AssetProviderConfig {

    String accessKey = ''
    CloudFileACL defaultFileACL = CloudFileACL.PublicRead
    String directory = ''
    String provider = 's3' // Default provider
    String region = ''
    String secretKey = ''
    String storagePath = ''
    String token = ''
    String endpoint = ''
    String baseUrl = ''
    Map<String,String> baseUrls = [:]
    String symmetricKey = ''
    String protocol = 'https'
    String proxyHost = ''
    Integer proxyPort = null
    String proxyUser = ''
    String proxyPassword = ''
    String proxyWorkstation = ''
    String proxyDomain = ''
    Integer maxConnections = 50
    Boolean keepAlive = false
    Boolean useGzip = false
    Boolean anonymous = false
    Boolean forceMultipart = false
    Boolean disableChunkedEncoding = false
    String tempDir

    Map toMap() {
        this.properties.findAll{ (it.key != 'class') }.collectEntries {
            [it.key, it.value]
        }
    }

    boolean validate() {
        if (!provider) {
            throw new InvalidUserDataException("Provider type required in config: 'provider' must be defined")
        }
        if (!accessKey || !secretKey) {
            throw new InvalidUserDataException("Credentials required in config: 'accessKey' and 'secretKey' must be defined")
        }
        if (!region || !directory || !storagePath) {
            throw new InvalidUserDataException("Remote provider storage info required in config: 'region', 'directory' and 'storagePath' must be defined")
        }
        true
    }

}
