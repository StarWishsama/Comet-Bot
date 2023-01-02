package ren.natsuyuk1.comet.service

import io.ktor.http.*
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusicDifficulty.musicDiffDatabase
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiRank.rankSeasonInfo
import ren.natsuyuk1.comet.objects.pjsk.local.pjskLocal
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

object ProjectSekaiManager {
    private var scope = ModuleScope("projectsekai_manager")

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope(scope.name(), parentContext)

        if (!pjskFolder.exists()) {
            pjskFolder.mkdir()
        }

        loadPJSKDatabase()
        refreshEvent()
        refreshCache()

        TaskManager.registerTask("pjsk_event", "0 14 * * *", ::refreshEvent)
        TaskManager.registerTaskDelayed(3.toDuration(DurationUnit.HOURS), ::refreshCache)
    }

    private suspend fun refreshCache() {
        ProjectSekaiData.updatePredictionData()
    }

    private suspend fun refreshEvent() {
        ProjectSekaiData.updateEventInfo()
    }

    fun getCurrentEventStatus(): SekaiEventStatus {
        val info = ProjectSekaiData.getCurrentEventInfo() ?: return SekaiEventStatus.ERROR
        val current = System.currentTimeMillis()
        val startAt = info.startTime
        val aggregateAt = info.aggregateTime

        return when (current) {
            in startAt..aggregateAt -> {
                SekaiEventStatus.ONGOING
            }

            in aggregateAt..aggregateAt + 600000 -> {
                SekaiEventStatus.COUNTING
            }

            else -> {
                SekaiEventStatus.END
            }
        }
    }

    private suspend fun loadPJSKDatabase() {
        pjskLocal.forEach {
            try {
                it.update()
                it.load()
            } catch (e: Exception) {
                logger.warn(e) { "加载文件 (${it.file.nameWithoutExtension}) 失败" }
            }
        }

        TaskManager.registerTask("pjsk_data_updater", "0 1 * * *") {
            pjskLocal.forEach {
                try {
                    if (it.update()) {
                        it.load()
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "尝试更新 ${it.file.nameWithoutExtension} 失败" }
                }
            }
        }
    }

    fun getSongLevel(songId: Int, difficulty: MusicDifficulty): Int? {
        val diffInfo =
            musicDiffDatabase.find { it.musicId == songId && it.musicDifficulty == difficulty } ?: return null

        return diffInfo.playLevel
    }

    fun getSongAdjustedLevel(songId: Int, difficulty: MusicDifficulty): Double? {
        val diffInfo =
            musicDiffDatabase.find { it.musicId == songId && it.musicDifficulty == difficulty } ?: return null

        return diffInfo.playLevel + diffInfo.playLevelAdjust
    }

    fun getLatestRankSeason(): Int? = rankSeasonInfo.lastOrNull()?.id

    private suspend fun downloadCardImage(assetBundleName: String, trainingStatus: String) {
        val suffix = if (trainingStatus == "done") "after_training" else "normal"
        /* ktlint-disable max-line-length */
        val url =
            "https://storage.sekai.best/sekai-assets/character/member_cutout/${assetBundleName}_rip/$suffix.png"
        /* ktlint-enable max-line-length */
        val cardFile = pjskFolder.resolve("cards/${assetBundleName}_$suffix.png")
        cardFile.touch()

        if (cardFile.exists() && cardFile.length() != 0L) {
            return
        }

        cometClient.client.downloadFile(url, cardFile) {
            it.contentType()?.match(ContentType.Image.PNG) == true
        }
    }

    suspend fun resolveCardImage(assetBundleName: String, trainingStatus: String): File {
        val suffix = if (trainingStatus == "done") "after_training" else "normal"
        val target = pjskFolder.resolve("cards/${assetBundleName}_$suffix.png")

        if (!target.exists() || target.length() == 0L) {
            downloadCardImage(assetBundleName, trainingStatus)

            logger.debug { "Downloaded pjsk card image (${assetBundleName}_$suffix)" }
        }

        return target
    }
}
