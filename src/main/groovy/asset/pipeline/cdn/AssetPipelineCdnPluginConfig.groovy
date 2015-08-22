package asset.pipeline.cdn

class AssetPipelineCdnPluginConfig extends AssetProviderConfig {

    def expires // Date or number of days
    String localStoragePath = 'assetCompile/assets/'
    List providers = []
    def gzip = '' // Boolean or 'both'

}
