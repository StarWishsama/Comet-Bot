package ren.natsuyuk1.comet.objects.pjsk.local

import io.ktor.client.statement.*
import io.ktor.http.*
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.PJSKMusicInfo
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.isType
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import java.io.File
import java.text.NumberFormat

object ProjectSekaiCharts {
    private val folder = pjskFolder.resolve("charts/")

    private fun getSdvxID(music: PJSKMusicInfo) =
        NumberFormat.getNumberInstance().apply {
            minimumIntegerDigits = 3
            maximumIntegerDigits = 3
        }.format(ProjectSekaiMusic.musicDatabase.values.sortedBy { it.publishedAt }.indexOf(music) + 1)

    private val validator: (HttpResponse) -> Boolean = { it.contentType()?.match(ContentType.Image.PNG) == true }

    suspend fun downloadChart(music: PJSKMusicInfo) {
        val chartFolder = folder.resolve("${music.id}/")

        if (chartFolder.exists() && !chartFolder.listFiles().isNullOrEmpty()) {
            return
        }

        val sdvxID = getSdvxID(music)

        chartFolder.mkdir()

        val bg = chartFolder.resolve("${music.id}bg.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/bg/${sdvxID}bg.png", bg, validator)
        val bar = chartFolder.resolve("${music.id}bar.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/bg/${sdvxID}bar.png", bar, validator)
        val chartMaster = chartFolder.resolve("${music.id}ma.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/obj/data${sdvxID}mst.png", chartMaster, validator)
        val chartExpert = chartFolder.resolve("${music.id}ex.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/obj/data${sdvxID}exp.png", chartExpert, validator)
    }

    fun getCharts(music: PJSKMusicInfo, difficulty: MusicDifficulty): Array<File> {
        val chartFolder = folder.resolve("${music.id}/")

        if (!chartFolder.isDirectory || chartFolder.listFiles()?.size != 4) {
            return emptyArray()
        }

        val bg = chartFolder.resolve("${music.id}bg.png").also { assert(it.isType("image/png")) }
        val bar = chartFolder.resolve("${music.id}bar.png").also { assert(it.isType("image/png")) }
        val chart = if (difficulty == MusicDifficulty.MASTER) {
            chartFolder.resolve("${music.id}ma.png")
        } else {
            chartFolder.resolve("${music.id}ex.png")
        }.also { assert(it.isType("image/png")) }

        return arrayOf(bg, bar, chart)
    }
}
