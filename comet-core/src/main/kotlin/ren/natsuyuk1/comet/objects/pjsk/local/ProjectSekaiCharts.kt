package ren.natsuyuk1.comet.objects.pjsk.local

import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKMusicInfo
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import java.io.File

object ProjectSekaiCharts {
    private val folder = pjskFolder.resolve("charts/")

    private fun getSdvxID(music: PJSKMusicInfo) =
        ProjectSekaiMusic.musicDatabase.values.sortedBy { it.publishedAt }.indexOf(music) + 1

    private suspend fun downloadChart(music: PJSKMusicInfo) {
        val chartFolder = folder.resolve("${music.id}/")

        if (chartFolder.exists() && !chartFolder.listFiles().isNullOrEmpty()) {
            return
        }

        val sdvxID = getSdvxID(music)

        chartFolder.mkdir()

        val bg = chartFolder.resolve("${music.id}bg.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/bg/${sdvxID}bg.png", bg)
        val bar = chartFolder.resolve("${music.id}bar.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/bg/${sdvxID}bar.png", bar)
        val chartMaster = chartFolder.resolve("${music.id}ma.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/obj/data${sdvxID}mst.png", chartMaster)
        val chartExpert = chartFolder.resolve("${music.id}ex.png").also { it.touch() }
        cometClient.client.downloadFile("https://sdvx.in/prsk/obj/data${sdvxID}exp.png", chartExpert)
    }

    suspend fun getCharts(music: PJSKMusicInfo, difficulty: MusicDifficulty): Array<File> {
        val chartFolder = folder.resolve("${music.id}/")

        if (!chartFolder.isDirectory || chartFolder.listFiles()?.size != 4) {
            downloadChart(music)
        }

        val bg = chartFolder.resolve("${music.id}bg.png")
        val bar = chartFolder.resolve("${music.id}bar.png")
        val chart = if (difficulty == MusicDifficulty.MASTER) {
            chartFolder.resolve("${music.id}ma.png")
        } else {
            chartFolder.resolve("${music.id}ex.png")
        }

        return arrayOf(bg, bar, chart)
    }
}
