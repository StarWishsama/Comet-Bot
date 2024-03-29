package ren.natsuyuk1.comet.service

import io.ktor.http.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.local.pjskLocal
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import java.io.File
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

object ProjectSekaiManager {
    private var scope = ModuleScope("projectsekai_manager")

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope(scope.name(), parentContext)

        if (!pjskFolder.exists()) {
            pjskFolder.mkdir()
        }

        loadPJSKDatabase()

        scope.launch { refreshEvent() }
        scope.launch { loadBest30Image() }

        TaskManager.registerTask("pjsk_event", "0 6 * * *", ::refreshEvent)

        logger.info { "Project Sekai 管理器加载完成" }
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

        TaskManager.registerTask("pjsk_data_updater", "0 17 * * *") {
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

    private suspend fun downloadCardImage(assetBundleName: String, trainingStatus: String) {
        val suffix = if (trainingStatus == "done") "after_training" else "normal"
        /* ktlint-disable max-line-length */
        val url =
            "https://storage.sekai.best/sekai-assets/thumbnail/chara_rip/${assetBundleName}_$suffix.png"
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

    private suspend fun loadBest30Image() {
        val b30 = pjskFolder.resolve("b30/")
        b30.mkdirs()
        val url = "https://raw.githubusercontent.com/StarWishsama/comet-resource-database/main/projectsekai/image/b30/"
        if (b30.listFiles().isNullOrEmpty()) {
            scope.launch {
                cometClient.client.downloadFile(
                    url + "AllPerfect.png",
                    b30.resolve("AllPerfect.png").also { it.touch() },
                )
            }

            scope.launch {
                cometClient.client.downloadFile(
                    url + "FullCombo.png",
                    b30.resolve("FullCombo.png").also { it.touch() },
                )
            }

            scope.launch {
                cometClient.client.downloadFile(
                    url + "b30-background.png",
                    b30.resolve("b30-background.png").also { it.touch() },
                )
            }
        }
    }
}
