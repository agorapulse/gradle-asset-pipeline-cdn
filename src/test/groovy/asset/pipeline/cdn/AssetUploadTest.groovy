package asset.pipeline.cdn

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import static org.junit.Assert.*

class AssetUploadTest {

    @Test
    public void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('uploadAssets', type: AssetUpload)

        assertTrue(task instanceof AssetUpload)
    }
}
