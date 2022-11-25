package ren.natsuyuk1.comet.test.draw

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.utils.file.absPath
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiAvatar {
    private val assetBundleName = "res018_no021"

    @Test
    fun testAvatarDownload() {
        runBlocking {
            val file = ProjectSekaiManager.resolveCardImage(assetBundleName)
            println(file.absPath)
            assertTrue(file.length() != 0L)
        }
    }
}
