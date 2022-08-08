package ren.natsuyuk1.comet.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Surface
import org.jetbrains.skia.paragraph.*
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.ProjectSekaiAPI.getRankPredictionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiEventStatus
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKMusicInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.profile.PJSKMusicDifficultyInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.SekaiBestPredictionInfo
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.*
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import java.awt.Color
import java.io.File
import java.nio.file.Files
import kotlin.coroutines.CoroutineContext
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

        val musicDiffFile = File(pjskFolder, "musicDifficulties.json")

        suspend fun loadMusicDiff() {
            try {
                musicDiffDatabase.addAll(
                    json.decodeFromString(
                        ListSerializer(PJSKMusicDifficultyInfo.serializer()),
                        musicDiffFile.readTextBuffered()
                    )
                )
            } catch (e: SerializationException) {
                logger.warn(e) { "解析音乐数据时出现问题, 路径 ${musicDiffFile.absPath}" }
            }
        }

        if (musicDiffFile.exists()) {
            loadMusicDiff()
        } else {
            musicDiffFile.touch()
            scope.launch {
                cometClient.client.downloadFile("https://musics.pjsekai.moe/musicDifficulties.json", musicDiffFile)
                loadMusicDiff()
            }
        }

        /**
         * Load Sekai Music Info
         */

        val musicFile = File(pjskFolder, "musics.json")
        val musicURL = "https://raw.githubusercontent.com/Sekai-World/sekai-master-db-diff/main/musics.json"

        suspend fun loadMusic() {
            try {
                musicDatabase.addAll(
                    json.decodeFromString(
                        ListSerializer(PJSKMusicInfo.serializer()),
                        musicFile.readTextBuffered()
                    )
                )
            } catch (e: SerializationException) {
                logger.warn(e) { "解析音乐数据时出现问题" }
            }
        }

        GitHubApi.getSpecificFileCommits("Sekai-World", "sekai-master-db-diff", "musics.json")
            .onSuccess {
                val current = Clock.System.now()
                val lastUpdate = Instant.parse(it.first().commit.committer.date)

                if (!musicFile.exists() || lastUpdate > current) {
                    scope.launch {
                        musicFile.touch()
                        cometClient.client.downloadFile(musicURL, musicFile)
                        loadMusic()
                    }
                } else {
                    loadMusic()
                }
            }.onFailure {
                if (musicFile.exists()) {
                    loadMusic()
                } else {
                    scope.launch {
                        musicFile.touch()
                        cometClient.client.downloadFile(
                            "https://raw.githubusercontent.com/Sekai-World/sekai-master-db-diff/main/musics.json",
                            musicFile
                        )
                        loadMusic()
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

    fun drawB30(user: ProjectSekaiUserInfo.UserGameData, b30: List<ProjectSekaiUserInfo.MusicResult>): File {
        val surface = Surface.makeRasterN32Premul(650, 900)

        val fonts =
            FontCollection().setDynamicFontManager(TypefaceFontProvider()).setDefaultFontManager(FontMgr.default)

        surface.canvas.apply {
            clear(Color.WHITE.rgb)

            val builder = ParagraphBuilder(ParagraphStyle().apply {
                alignment = Alignment.LEFT
                textStyle = TextStyle().apply {
                    color = Color.BLACK.rgb
                    fontSize = 20f
                    fontStyle = FontStyle.NORMAL.withWeight(500)
                    fontFamilies = arrayOf("Hiragino Sans")
                }
            }, fonts)

            builder.addText("${user.userGameData.name} - ${user.userGameData.userID} - BEST 30\n")

            builder.popStyle().pushStyle(TextStyle().apply {
                color = Color.BLACK.rgb
                fontSize = 18f
                fontStyle = FontStyle.NORMAL.withWeight(500)
                fontFamilies = arrayOf("Hiragino Sans")
            })

            builder.addText("\n")

            b30.forEach { mr ->
                val status = if (mr.isAllPerfect) "AP" else "FC"
                val multiplier = if (mr.isAllPerfect) 1 else 0.95
                builder.addText(
                    "${getSongName(mr.musicId)} [${mr.musicDifficulty.name.uppercase()} ${
                        getSongLevel(
                            mr.musicId,
                            mr.musicDifficulty
                        )
                    }] $status (${getSongAdjustedLevel(mr.musicId, mr.musicDifficulty)?.fixDisplay(1)}x$multiplier)\n"
                )
            }

            builder.addText("\n")

            builder.popStyle().pushStyle(TextStyle().apply {
                color = Color.BLACK.rgb
                fontSize = 13f
                fontStyle = FontStyle.NORMAL.withWeight(500)
                fontFamilies = arrayOf("Hiragino Sans")
            })

            builder.addText("Generated by Comet | 数据来源于 profile.pjsekai.moe")

            builder.build().layout(700f).paint(this, 10f, 10f)
        }

        val image = surface.makeImageSnapshot()

        val imageData = image.encodeToData(EncodedImageFormat.PNG)
        val tmpFile = File(cacheDirectory, "${System.currentTimeMillis()}.png").apply {
            deleteOnExit()
        }

        imageData?.bytes?.let {
            Files.write(tmpFile.toPath(), it)
        }

        return tmpFile
    }
}
