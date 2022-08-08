package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.SekaiBestPredictionInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

object ProjectSekaiManager {
    lateinit var predictionCache: SekaiBestPredictionInfo
        private set

    private var scope = ModuleScope("projectsekai_helper")

    private val pjskFolder = resolveResourceDirectory("./projectsekai")

    private lateinit var carnivalTeamI18NCache: JsonObject

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("projectsekai_helper", parentContext)

        scope.launch { refreshCache() }

        if (!pjskFolder.exists()) {
            pjskFolder.mkdir()
        }

        fetchI18NFile()

        TaskManager.registerTask(20.minutes, ::refreshCache)
    }

    private fun refreshCache() {
        predictionCache = runBlocking { cometClient.getRankPredictionInfo() }
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

    private suspend fun fetchI18NFile() {
        if (!pjskFolder.exists()) {
            pjskFolder.mkdir()
        }

        GitHubApi.getSpecificFileCommits("Sekai-World", "sekai-i18n", "zh-TW/cheerful_carnival_teams.json")
            .onSuccess {
                val commitTime = Instant.parse(it.first().commit.committer.date)
                val now = Clock.System.now()
                val teams = File(pjskFolder, "cheerful_carnival_teams.json")

                teams.touch()

                carnivalTeamI18NCache = if (teams.readTextBuffered().isEmpty() || commitTime > now) {
                    scope.launch {
                        teams.touch()
                        cometClient.client.downloadFile(
                            "https://raw.githubusercontent.com/Sekai-World/sekai-i18n/main/zh-TW/cheerful_carnival_teams.json",
                            teams
                        )
                    }

                    json.parseToJsonElement(teams.readTextBuffered()).jsonObject
                } else {
                    json.parseToJsonElement(teams.readTextBuffered()).jsonObject
                }
            }.onFailure {
                logger.warn(it) { "加载 Project Sekai 本地化文件失败!" }
            }
    }

    fun getCarnivalTeamI18nName(teamId: Int): String? {
        if (!::carnivalTeamI18NCache.isInitialized) {
            return null
        }

        val currentEvent = carnivalTeamI18NCache.filter { (k, _) -> k == teamId.toString() }

        return if (currentEvent.isEmpty()) {
            null
        } else {
            return currentEvent.values.firstOrNull()?.jsonPrimitive?.content
        }
    }
}
