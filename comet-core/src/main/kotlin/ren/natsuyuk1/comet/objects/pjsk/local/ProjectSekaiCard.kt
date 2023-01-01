package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.serialization.builtins.ListSerializer
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.profile.PJSKCard
import ren.natsuyuk1.comet.util.pjsk.getSekaiResourceURL
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

object ProjectSekaiCard : ProjectSekaiLocalFile(
    pjskFolder.resolve("cards.json"),
    5.days
) {
    private val cards = mutableListOf<PJSKCard>()
    private val url by lazy { getSekaiResourceURL("cards.json") }

    override suspend fun load() {
        file.touch()

        try {
            cards.addAll(
                json.decodeFromString(
                    ListSerializer(PJSKCard.serializer()),
                    file.readTextBuffered()
                )
            )
        } catch (e: Exception) {
            logger.warn(e) { "解析卡面数据时出现问题" }
        }
    }

    override suspend fun update(): Boolean {
        if (!file.exists() || file.length() == 0L || isOutdated()) {
            if (cometClient.client.downloadFile(url, file)) {
                updateLastUpdateTime()
                logger.info { "成功更新卡面数据" }
                return true
            } else {
                logger.warn { "尝试更新卡面数据失败" }
            }
        }

        return false
    }

    fun getAssetBundleName(id: Int): String? = cards.find { it.id == id }?.assetBundleName
}
