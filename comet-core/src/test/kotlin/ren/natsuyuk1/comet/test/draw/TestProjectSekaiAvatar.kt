package ren.natsuyuk1.comet.test.draw

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.utils.file.absPath
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiAvatar {
    @Test
    fun testAvatarDownload() {
        runBlocking {
            val f = ProjectSekaiManager.resolveCardImage("res018_no021")
            println(f.absPath)
            assertTrue(f.length() != 0L)
        }
    }
}
