package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.serialization.builtins.ListSerializer
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.official.PJSKMusicInfo
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import ren.natsuyuk1.comet.utils.string.ldSimilarity
import java.io.File
import java.math.BigDecimal
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

object ProjectSekaiMusic : ProjectSekaiLocalFile(
    pjskFolder.resolve("musics.json"),
    1.days
) {
    private const val url = "https://musics.pjsekai.moe/musics.json"
    private val musicCoverFolder = pjskFolder.resolve("covers/")

    internal val musicDatabase = mutableMapOf<Int, PJSKMusicInfo>()

    override suspend fun load() {
        try {
            val content = file.readTextBuffered()
            if (content.isBlank()) {
                logger.warn { "加载 Project Sekai 歌曲数据失败, 文件为空" }
            } else {
                val music = json.decodeFromString(
                    ListSerializer(PJSKMusicInfo.serializer()),
                    content
                )

                music.forEach {
                    musicDatabase[it.id] = it
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "解析 歌曲数据时出现问题" }
        }
    }

    override suspend fun update(): Boolean {
        file.touch()

        if (file.length() == 0L || checkOutdated()) {
            cometClient.client.downloadFile(url, file)
            updateLastUpdateTime()

            return true
        }

        return false
    }

    fun getMusicInfo(name: String): PJSKMusicInfo? = musicDatabase.values.find { it.title == name }

    fun getMusicInfo(id: Int): PJSKMusicInfo? = musicDatabase[id]

    fun fuzzyGetMusicInfo(name: String): Pair<PJSKMusicInfo, BigDecimal>? {
        val entry = musicDatabase.values.filter {
            val alias = ProjectSekaiMusicAlias.getAlias(it.id) ?: emptyArray()
            ldSimilarity(it.title, name)
                .max(
                    alias.maxOfOrNull { a -> ldSimilarity(a, name) }
                        ?: BigDecimal.ZERO
                ) > BigDecimal.valueOf(0.4)
        }.associateWith { ldSimilarity(it.title, name) }.entries.firstOrNull()

        return if (entry == null) {
            null
        } else {
            Pair(entry.key, entry.value)
        }
    }

    suspend fun getMusicCover(music: PJSKMusicInfo): File {
        if (!musicCoverFolder.exists()) {
            musicCoverFolder.mkdir()
        }

        val cover = musicCoverFolder.resolve(music.assetBundleName + ".png")

        if (!cover.exists()) {
            downloadMusicCover(music)
        }

        return cover
    }

    private suspend fun downloadMusicCover(music: PJSKMusicInfo) {
        if (!musicCoverFolder.exists()) {
            musicCoverFolder.mkdir()
        }

        val cover = musicCoverFolder.resolve(music.assetBundleName + ".png")
        cover.touch()

        val url =
            "https://assets.pjsek.ai/file/pjsekai-assets/startapp/music/jacket/${music.assetBundleName}/${music.assetBundleName}.png" // ktlint-disable max-line-length
        cometClient.client.downloadFile(url, cover)
    }
}
