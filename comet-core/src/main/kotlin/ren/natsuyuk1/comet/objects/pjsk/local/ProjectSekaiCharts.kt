package ren.natsuyuk1.comet.objects.pjsk.local

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicID
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProfileMusicInfo
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.ktor.isOK
import java.io.File
import java.text.NumberFormat

object ProjectSekaiCharts {
    private val scope = ModuleScope("pjsk_charts", dispatcher = Dispatchers.IO)
    private val chartDir = pjskFolder.resolve("charts/")
    private val processCharts = mutableSetOf<Int>()

    // SDVX ID format, 3 digit number id
    private val sdvxFormat by lazy {
        NumberFormat.getNumberInstance().apply {
            minimumIntegerDigits = 3
            maximumIntegerDigits = 3
        }
    }

    private fun getSdvxID(music: ProfileMusicInfo) =
        sdvxFormat.format(PJSKProfileMusic.musicDatabase.values.sortedBy { it.publishedAt }.indexOf(music) + 1)

    private val validator: (HttpResponse) -> Boolean = { it.contentType()?.match(ContentType.Image.PNG) == true }

    suspend fun downloadChart(music: ProfileMusicInfo): DownloadStatus {
        val musicChartDir = chartDir.resolve("${music.id}/")

        if (musicChartDir.exists() && !musicChartDir.listFiles().isNullOrEmpty()) {
            return DownloadStatus.OK
        }

        val sdvxID = getSdvxID(music)

        processCharts.add(music.id)

        musicChartDir.mkdirs()

        val bg = musicChartDir.resolve("${music.id}bg.png").also { it.touch() }
        val bar = musicChartDir.resolve("${music.id}bar.png").also { it.touch() }
        val chartMaster = musicChartDir.resolve("${music.id}ma.png").also { it.touch() }
        val chartExpert = musicChartDir.resolve("${music.id}ex.png").also { it.touch() }

        scope.apply {
            val bgResult =
                async { cometClient.client.downloadFile("https://sdvx.in/prsk/bg/${sdvxID}bg.png", bg, validator) }
            val barResult =
                async { cometClient.client.downloadFile("https://sdvx.in/prsk/bg/${sdvxID}bar.png", bar, validator) }
            val mstResult = async {
                cometClient.client.downloadFile(
                    "https://sdvx.in/prsk/obj/data${sdvxID}mst.png",
                    chartMaster,
                    validator
                )
            }
            val expResult = async {
                cometClient.client.downloadFile(
                    "https://sdvx.in/prsk/obj/data${sdvxID}exp.png",
                    chartExpert,
                    validator
                )
            }

            val status = arrayOf(bgResult.await(), barResult.await(), mstResult.await(), expResult.await())

            return status.find { !it.isOK() } ?: DownloadStatus.OK
        }
    }
}

internal fun MusicID.bg(): File =
    pjskFolder.resolve("charts/$this/${this}bg.png")

internal fun MusicID.bar(): File =
    pjskFolder.resolve("charts/$this/${this}bar.png")

internal fun MusicID.chartKey(difficulty: MusicDifficulty): File =
    pjskFolder.resolve("charts/$this/${this}${difficulty.name.take(2).lowercase()}.png")

internal fun MusicID.chart(difficulty: MusicDifficulty): File =
    pjskFolder.resolve("charts/$this/chart_$difficulty.png")
