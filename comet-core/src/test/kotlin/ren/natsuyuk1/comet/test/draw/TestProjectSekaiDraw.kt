package ren.natsuyuk1.comet.test.draw

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getUserEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiDataTable
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiLocalFileTable
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
import java.io.File
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
    fun testAvatarDraw() {
        if (isCI()) return

        initTestDatabase()

        runBlocking {
            DatabaseManager.loadTables(ProjectSekaiLocalFileTable, ProjectSekaiDataTable)
            SkikoHelper.loadSkiko()
            ProjectSekaiManager.init(EmptyCoroutineContext)

            val info = cometClient.getUserEventInfo(eventID, id)

            val res = newSuspendedTransaction {
                info.drawEventInfo(eventID)
            }

            val tfile = File.createTempFile("pjsk_userinfo_test_", ".png")
            res.find<Image>()?.stream?.use {
                tfile.outputStream().use { fos ->
                    it.copyTo(fos)
                }
            }

            println(tfile.absPath)
        }
    }

    @Test
    fun testChartDraw() {
        if (isCI()) return

        runBlocking {
            SkikoHelper.loadSkiko()
            ProjectSekaiMusic.update()
            ProjectSekaiMusic.load()
            ProjectSekaiMusicDifficulty.update()
            ProjectSekaiMusicDifficulty.load()

            // Represent to project sekai music named `Iなんです`
            val music = ProjectSekaiMusic.getMusicInfo(139)
            assertNotNull(music)
            val test = cacheDirectory.resolve("test.png")
            test.touch()

            val image = ProjectSekaiImageService.drawCharts(music, MusicDifficulty.MASTER)

            image?.stream?.use { istr ->
                test.outputStream().use {
                    istr.copyTo(it)
                }
            }

            println(test.absPath)

            assertTrue { test.length() != 0L }
        }
    }
}
