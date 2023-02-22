package ren.natsuyuk1.comet.test.draw

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusic
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.service.image.ProjectSekaiImageService
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiDraw {
    // Represent to card named 何かが違う食卓
    private val assetBundleName = "res018_no021"

    private val eventID = 81

    // Welcome to add me as friend :D
    private val id = 210043933010767872L

    @BeforeAll
    fun init() {
        runBlocking {
            SkikoHelper.loadSkiko()
            ProjectSekaiManager.init(EmptyCoroutineContext)
        }
    }

    @Test
    fun testAvatarDownload() {
        if (isCI()) return

        runBlocking {
            val file = ProjectSekaiManager.resolveCardImage(assetBundleName, "done")
            println(file.absPath)
            assertTrue(file.length() != 0L)
        }
    }

    @Test
    fun testChartDraw() {
        if (isCI()) return

        runBlocking {
            // Represent to music named `気まぐれメルシィ`
            val music = ProjectSekaiMusic.getMusicInfo(281)
            assertNotNull(music)

            val (image, _) = ProjectSekaiImageService.drawCharts(music, MusicDifficulty.MASTER)

            println(image)

            assertTrue { image?.length() != 0L }
        }
    }
}
