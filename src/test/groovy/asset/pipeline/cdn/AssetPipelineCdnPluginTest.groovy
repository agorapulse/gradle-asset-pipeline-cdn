package asset.pipeline.cdn

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import static org.junit.Assert.*

class AssetPipelineCdnPluginTest {

    @Test
    public void assetPipelineCdnPluginAddsUploadTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'asset-pipeline-cdn'

        assertTrue(project.tasks.uploadAssets instanceof AssetUpload)
    }
}
