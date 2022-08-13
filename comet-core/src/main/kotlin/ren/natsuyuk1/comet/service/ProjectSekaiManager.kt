package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKMusicInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKRankSeasonInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.profile.PJSKMusicDifficultyInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.SekaiBestPredictionInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.cacheDirectory
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.skiko.FontUtil
import java.awt.Color
import java.io.File
import java.nio.file.Files
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

object ProjectSekaiManager {
    lateinit var predictionCache: SekaiBestPredictionInfo
        private set

    private var scope = ModuleScope("projectsekai_helper")

    private val pjskFolder = resolveResourceDirectory("./projectsekai")

    private lateinit var carnivalTeamI18NCache: JsonObject

    val musicDiffDatabase = mutableListOf<PJSKMusicDifficultyInfo>()

    val musicDatabase = mutableListOf<PJSKMusicInfo>()

    private val rankSeasonInfo = mutableListOf<PJSKRankSeasonInfo>()

    suspend fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("projectsekai_helper", parentContext)

        loadPJSKDatabase()

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

                if (teams.readTextBuffered().isEmpty() || commitTime > now) {
                    scope.launch {
                        cometClient.client.downloadFile(
                            "https://raw.githubusercontent.com/Sekai-World/sekai-i18n/main/zh-TW/cheerful_carnival_teams.json",
                            teams
                        )
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

    private suspend fun loadPJSKDatabase() {

        /**
         * Load Sekai Music Difficulties Info
         */
        loadSpecificDatabase("musicDifficulties.json", PJSKMusicDifficultyInfo.serializer(), musicDiffDatabase)

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
                database.addAll(
                    json.decodeFromString(
                        ListSerializer(serializer),
                        file.readTextBuffered()
                    )
                )

                logger.info { "已加载 Project Sekai $fileName 数据" }
            } catch (e: SerializationException) {
                logger.warn(e) { "解析 $fileName 数据时出现问题" }
            }
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

    fun drawB30(user: ProjectSekaiUserInfo.UserGameData, b30: List<ProjectSekaiUserInfo.MusicResult>): File {
        val surface = Surface.makeRasterN32Premul(650, 900)

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            ParagraphBuilder(ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = FontUtil.defaultFontStyle(Color.BLACK, 20f)
            }, FontUtil.fonts).apply {
                addText("${user.userGameData.name} - ${user.userGameData.userID} - BEST 30\n")

                popStyle().pushStyle(FontUtil.defaultFontStyle(Color.BLACK, 18f))

                addText("\n")

                b30.forEach { mr ->
                    val status = if (mr.isAllPerfect) "AP" else "FC"

                    addText(
                        "${getSongName(mr.musicId)} [${mr.musicDifficulty.name.uppercase()} ${
                            getSongLevel(
                                mr.musicId,
                                mr.musicDifficulty
                            )
                        }] $status (${
                            getSongAdjustedLevel(
                                mr.musicId,
                                mr.musicDifficulty
                            )?.fixDisplay(1)
                        })\n"
                    )
                }

                addText("\n")

                popStyle().pushStyle(FontUtil.defaultFontStyle(Color.BLACK, 13f))

                addText("由 Comet 生成 | 数据来源于 profile.pjsekai.moe")
            }.build().layout(650f).paint(this, 10f, 10f)
        }

        val image = surface.makeImageSnapshot()

        val tmpFile = File(cacheDirectory, "${System.currentTimeMillis()}.png").apply {
            TaskManager.registerTaskDelayed(1.hours) {
                delete()
            }
        }

        image.encodeToData(EncodedImageFormat.PNG)?.bytes?.let {
            Files.write(tmpFile.toPath(), it)
        }

        return tmpFile
    }
}
