package asset.pipeline.cdn

class AssetPipelineCdnPluginExtension extends AssetProviderConfig {

    def expires // Date or number of days
    String localStoragePath = 'assets/'
    List providers = []
    def gzip = '' // Boolean or 'both'

}
