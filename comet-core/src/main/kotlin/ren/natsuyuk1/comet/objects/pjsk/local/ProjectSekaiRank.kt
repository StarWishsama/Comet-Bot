package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.PJSKRankSeasonInfo
import ren.natsuyuk1.comet.service.RateLimitAPI
import ren.natsuyuk1.comet.service.RateLimitService
import ren.natsuyuk1.comet.util.pjsk.getSekaiBestResourceURL
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.lastModifiedTime
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

object ProjectSekaiRank : ProjectSekaiLocalFile(
    pjskFolder.resolve("rankMatchSeasons.json"), 30.days
) {
    internal val rankSeasonInfo = mutableListOf<PJSKRankSeasonInfo>()

    override suspend fun load() {
        try {
            rankSeasonInfo.addAll(
                json.decodeFromString(
                    ListSerializer(PJSKRankSeasonInfo.serializer()), file.readTextBuffered()
                )
            )
        } catch (e: Exception) {
            logger.warn(e) { "解析排位数据时出现问题" }
        }
    }

    override suspend fun update(): Boolean {
        if (RateLimitService.isRateLimit(RateLimitAPI.GITHUB)) {
            return false
        }

        GitHubApi.getSpecificFileCommits("Sekai-World", "sekai-master-db-diff", file.name).onSuccess {
            val current = file.lastModifiedTime()
            val lastUpdate = Instant.parse(it.first().commit.committer.date)

            if (!file.exists() || lastUpdate > current) {
                file.touch()
                if (cometClient.client.downloadFile(
                        getSekaiBestResourceURL(file.name), file
                    ) == DownloadStatus.OK
                ) {
                    logger.info { "成功更新排位数据" }
                    return true
                }
            }
        }

        return false
    }
}
