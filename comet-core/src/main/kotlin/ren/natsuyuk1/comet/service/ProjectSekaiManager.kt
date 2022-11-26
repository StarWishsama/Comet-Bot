package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKMusicInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKRankSeasonInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.profile.PJSKCard
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.profile.PJSKMusicDifficultyInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.absPath
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

object ProjectSekaiManager {
    private var scope = ModuleScope("projectsekai_helper")

    private val pjskFolder = resolveResourceDirectory("./projectsekai")

    private lateinit var carnivalTeamI18NCache: JsonObject

    val musicDiffDatabase = mutableListOf<PJSKMusicDifficultyInfo>()

    val musicDatabase = mutableListOf<PJSKMusicInfo>()

    private val rankSeasonInfo = mutableListOf<PJSKRankSeasonInfo>()

    private val cards = mutableListOf<PJSKCard>()

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("projectsekai_helper", parentContext)

        loadPJSKDatabase()

        if (!pjskFolder.exists()) {
            pjskFolder.mkdir()
        }

        fetchI18NFile()

        TaskManager.registerTaskDelayed(3.toDuration(DurationUnit.HOURS), ::refreshCache)
        TaskManager.registerTaskDelayed(1.toDuration(DurationUnit.DAYS), ::refreshEvent)
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

                if (teams.readTextBuffered().isEmpty() || commitTime > now) {
                    scope.launch {
                        /* ktlint-disable max-line-length */
                        cometClient.client.downloadFile(
                            "https://raw.githubusercontent.com/Sekai-World/sekai-i18n/main/zh-TW/cheerful_carnival_teams.json",
                            teams
                        )
                        /* ktlint-enable max-line-length */
                        carnivalTeamI18NCache = json.parseToJsonElement(teams.readTextBuffered()).jsonObject
                    }
                } else {
                    carnivalTeamI18NCache = json.parseToJsonElement(teams.readTextBuffered()).jsonObject
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

    suspend fun loadCards() {
        val cardsFile = pjskFolder.resolve("cards.json")

        try {
            cards.addAll(
                json.decodeFromString(
                    ListSerializer(PJSKCard.serializer()),
                    cardsFile.readTextBuffered()
                )
            )
        } catch (e: Exception) {
            logger.warn(e) { "解析卡面数据时出现问题, 路径 ${cardsFile.absPath}" }
        }
    }

    private suspend fun loadPJSKDatabase() {
        /**
         * Load Sekai Music Difficulties Info
         */

        val musicDiffFile = File(pjskFolder, "musicDifficulties.json")

        suspend fun loadMusicDiff() {
            try {
                musicDiffDatabase.addAll(
                    json.decodeFromString(
                        ListSerializer(PJSKMusicDifficultyInfo.serializer()),
                        musicDiffFile.readTextBuffered()
                    )
                )
            } catch (e: Exception) {
                logger.warn(e) { "解析音乐数据时出现问题, 路径 ${musicDiffFile.absPath}" }
            }
        }

        if (musicDiffFile.exists()) {
            scope.launch {
                loadMusicDiff()
            }
        } else {
            musicDiffFile.touch()
            scope.launch {
                cometClient.client.downloadFile("https://musics.pjsekai.moe/musicDifficulties.json", musicDiffFile)
                loadMusicDiff()
            }
        }

        val cardsFile = pjskFolder.resolve("cards.json")

        if (cardsFile.exists()) {
            scope.launch {
                loadCards()
            }
        } else {
            cardsFile.touch()
            scope.launch {
                cometClient.client.downloadFile(
                    "https://gitlab.com/pjsekai/database/jp/-/raw/main/cards.json",
                    cardsFile
                )
                loadCards()
            }
        }

        /**
         * Load Sekai Music Info
         */
        loadSpecificDatabase("musics.json", PJSKMusicInfo.serializer(), musicDatabase)

        /**
         * Load rank season info
         */
        loadSpecificDatabase("rankMatchSeasons.json", PJSKRankSeasonInfo.serializer(), rankSeasonInfo)
    }

    private suspend fun <T> loadSpecificDatabase(
        fileName: String,
        serializer: KSerializer<T>,
        database: MutableList<T>
    ) {
        val file = File(pjskFolder, fileName)
        val url = "https://raw.githubusercontent.com/Sekai-World/sekai-master-db-diff/main/$fileName"

        suspend fun loadDatabase() {
            try {
                val content = file.readTextBuffered()
                if (content.isBlank()) {
                    logger.warn { "加载 Project Sekai $fileName 失败, 文件为空" }
                } else {
                    database.addAll(
                        json.decodeFromString(
                            ListSerializer(serializer),
                            content
                        )
                    )

                    logger.info { "已加载 Project Sekai $fileName 数据" }
                }
            } catch (e: Exception) {
                logger.warn(e) { "解析 $fileName 数据时出现问题" }
            }
        }

        suspend fun load() {
            if (file.exists()) {
                loadDatabase()
            } else {
                scope.launch {
                    file.touch()
                    cometClient.client.downloadFile(url, file)
                    loadDatabase()
                }
            }
        }

        if (RateLimitService.isRateLimit(RateLimitAPI.GITHUB)) {
            load()
            return
        }

        GitHubApi.getSpecificFileCommits("Sekai-World", "sekai-master-db-diff", fileName)
            .onSuccess {
                val current = Clock.System.now()
                val lastUpdate = Instant.parse(it.first().commit.committer.date)

                if (!file.exists() || lastUpdate > current) {
                    scope.launch {
                        file.touch()
                        cometClient.client.downloadFile(url, file)
                        loadDatabase()
                    }
                } else {
                    loadDatabase()
                }
            }.onFailure {
                load()
            }
    }

    fun getSongIdByName(name: String): Int? {
        val songInfo = musicDatabase.find { it.title == name } ?: return null

        return songInfo.id
    }

    fun getSongName(id: Int): String? {
        val songInfo = musicDatabase.find { it.id == id } ?: return null

        return songInfo.title
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

    fun getAssetBundleName(id: Int): String? = cards.find { it.id == id }?.assetBundleName

    suspend fun downloadCardImage(assetBundleName: String) {
        /* ktlint-disable max-line-length */
        val url =
            "https://assets.pjsek.ai/file/pjsekai-assets/startapp/character/member_cutout/$assetBundleName/normal/thumbnail_xl.png"
        /* ktlint-enable max-line-length */
        val cardFile = pjskFolder.resolveSibling("cards/$assetBundleName.png")
        cardFile.touch()

        if (cardFile.exists() && cardFile.length() != 0L) {
            return
        }

        cometClient.client.downloadFile(url, cardFile)
    }

    suspend fun resolveCardImage(assetBundleName: String): File {
        val target = pjskFolder.resolveSibling("cards/$assetBundleName.png")

        if (!target.exists() || target.length() == 0L) {
            downloadCardImage(assetBundleName)

            logger.debug { "Downloaded pjsk card image ($assetBundleName)" }
        }

        return target
    }

    suspend fun reload() {
        loadPJSKDatabase()
    }
}
