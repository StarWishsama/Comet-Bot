package ren.natsuyuk1.comet.test.draw

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.sink
import okio.source
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import ren.natsuyuk1.comet.utils.file.touch
import java.io.File
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiAvatar {
    private val assetBundleName = "res018_no021"

    @Test
    fun testAvatarDownloadAltar() {
        runBlocking {
            /* ktlint-disable max-line-length */
            val url =
                "https://assets.pjsek.ai/file/pjsekai-assets/startapp/character/member_cutout/$assetBundleName/normal/thumbnail_xl.png"
            /* ktlint-enable max-line-length */
            val file = File(File(resolveResourceDirectory("./projectsekai"), "cards"), "$assetBundleName.png")
            file.touch()

            cometClient.client.apply {
                val req = get(url)
                val resp = req.body<InputStream>()

                resp.source().buffer().use { i ->
                    file.sink().buffer().use { o ->
                        o.writeAll(i)
                    }
                }

                println(file.absPath)
                assertTrue(file.length() != 0L)
            }
        }
    }

    @Test
    fun testAvatarDownload() {
        runBlocking {
            val file = ProjectSekaiManager.resolveCardImage(assetBundleName)
            println(file.absPath)
            assertTrue(file.length() != 0L)
        }
    }
}
