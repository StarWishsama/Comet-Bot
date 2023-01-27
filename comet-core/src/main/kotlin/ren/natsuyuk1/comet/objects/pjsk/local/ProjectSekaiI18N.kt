package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.util.pjsk.getCometDatabaseURL
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.lastModifiedTime
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

object ProjectSekaiI18N : ProjectSekaiLocalFile(
    pjskFolder.resolve("cheerful_carnival_teams.json"),
    3.days
) {
    private var carnivalTeamName: JsonObject? = null
    override suspend fun load() {
        carnivalTeamName = json.parseToJsonElement(file.readTextBuffered()).jsonObject
    }

    override suspend fun update(): Boolean {
        GitHubApi.getSpecificFileCommits(
            "StarWishsama",
            "comet-resource-database",
            "projectsekai/cheerful_carnival_teams.json"
        )
            .onSuccess {
                val commitTime = Instant.parse(it.first().commit.committer.date)
                val lastModified = file.lastModifiedTime()

                file.touch()

                if (file.length() == 0L || commitTime > lastModified) {
                    if (cometClient.client.downloadFile(
                            getCometDatabaseURL("cheerful_carnival_teams.json"),
                            file
                        ) == DownloadStatus.OK
                    ) {
                        logger.info { "成功更新本地化数据" }

                        return true
                    }
                }
            }.onFailure {
                logger.warn(it) { "加载 Project Sekai 本地化文件失败!" }
            }

        return false
    }

    fun getCarnivalTeamName(teamId: Int): String? {
        if (carnivalTeamName == null) {
            return null
        }

        val currentEvent = carnivalTeamName!!.filter { (k, _) -> k == teamId.toString() }

        return if (currentEvent.isEmpty()) {
            null
        } else {
            return currentEvent.values.firstOrNull()?.jsonPrimitive?.content
        }
    }
}
