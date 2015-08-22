package asset.pipeline.cdn

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
