package ren.natsuyuk1.comet.test.draw

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiCard
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusic
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusicDifficulty
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.service.image.ProjectSekaiImageService
import ren.natsuyuk1.comet.service.image.ProjectSekaiImageService.drawEventInfo
import ren.natsuyuk1.comet.test.initTestDatabase
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiDraw {
    // Represent to card named 何かが違う食卓
    private val assetBundleName = "res018_no021"

    // Represent to event named `Echo my melody`
    private val eventID = 77

    // Welcome to add me as friend :D
    private val id = 210043933010767872L

    @Test
    fun testAvatarDownload() {
        if (isCI()) return

        runBlocking {
            val file = ProjectSekaiManager.resolveCardImage(assetBundleName)
            println(file.absPath)
            assertTrue(file.length() != 0L)
        }
    }

    @Test
    fun testAvatarDraw() {
        if (isCI()) return

        initTestDatabase()

        runBlocking {
            SkikoHelper.loadSkiko()
            ProjectSekaiCard.load()

            val info = cometClient.getUserEventInfo(eventID, id)

            val res = newSuspendedTransaction {
                info.drawEventInfo(eventID)
            }

            println(res.find<Image>())
        }
    }

    @Test
    fun testChartDraw() {
        if (isCI()) return

        runBlocking {
            SkikoHelper.loadSkiko()
            ProjectSekaiMusic.load()
            ProjectSekaiMusicDifficulty.load()

            // Represent to project sekai music named `Iなんです`
            val music = ProjectSekaiMusic.getMusicInfo(304)
            assertNotNull(music)
            val test = cacheDirectory.resolve("test.png")
            test.touch()

            val wrapper = ProjectSekaiImageService.drawCharts(music, MusicDifficulty.MASTER)

            wrapper.find<Image>()?.stream?.use { istr ->
                test.outputStream().use {
                    istr.copyTo(it)
                }
            }

            println(test.absPath)

            assertTrue { test.length() != 0L }
        }
    }
}
