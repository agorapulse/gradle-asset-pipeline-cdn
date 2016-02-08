package asset.pipeline.cdn

import org.gradle.api.Project
import org.gradle.api.Plugin

class AssetPipelineCdnPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('assetsCdn', AssetPipelineCdnPluginExtension)
        project.task('uploadAssets', description: 'Uploads static assets to a CDN directory/bucket.', type: AssetUpload)
    }
}
