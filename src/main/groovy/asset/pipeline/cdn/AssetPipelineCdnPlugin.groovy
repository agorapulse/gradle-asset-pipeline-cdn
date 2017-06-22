package asset.pipeline.cdn

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task

class AssetPipelineCdnPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('assetsCdn', AssetPipelineCdnPluginExtension)
        Task assetUpload = project.task('uploadAssets', description: 'Uploads static assets to a CDN directory/bucket.', type: AssetUpload)
        project.tasks.whenTaskAdded { Task t ->
            if (t.name == 'assetCompile') {
                assetUpload.mustRunAfter(t)
            }
        }
    }
}
