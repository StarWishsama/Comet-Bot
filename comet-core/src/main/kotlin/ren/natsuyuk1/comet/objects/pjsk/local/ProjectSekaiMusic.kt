package ren.natsuyuk1.comet.objects.pjsk.local

import io.ktor.http.*
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
import ren.natsuyuk1.comet.utils.string.normalize
import java.io.File
import java.math.BigDecimal
import java.text.Normalizer
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

        if (file.length() == 0L || isOutdated()) {
            if (cometClient.client.downloadFile(url, file)) {
                updateLastUpdateTime()
                logger.info { "成功更新音乐数据" }
                return true
            }
        }

        return false
    }

    fun getMusicInfo(name: String): PJSKMusicInfo? = musicDatabase.values.find { it.title == name }

    fun getMusicInfo(id: Int): PJSKMusicInfo? = musicDatabase[id]

    fun fuzzyGetMusicInfo(name: String): Pair<PJSKMusicInfo, BigDecimal>? {
        val name = name.normalize(Normalizer.Form.NFKC)

        return musicDatabase.values.associateWith {
            val alias = ProjectSekaiMusicAlias.getAlias(it.id) ?: emptyArray()
            val sim = ldSimilarity(it.title.normalize(Normalizer.Form.NFKC), name)
                .max(
                    alias.maxOfOrNull { a -> ldSimilarity(a.normalize(Normalizer.Form.NFKC), name) }
                        ?: BigDecimal.ZERO
                )

            sim
        }.filter { it.value > BigDecimal.valueOf(0.4) }
            .maxByOrNull { it.value }
            ?.toPair()
    }

    suspend fun getMusicCover(music: PJSKMusicInfo): File {
        if (!musicCoverFolder.exists()) {
            musicCoverFolder.mkdir()
        }

        val cover = musicCoverFolder.resolve(music.assetBundleName + ".png")

        if (!cover.exists() || cover.length() == 0L) {
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

        cometClient.client.downloadFile(url, cover) {
            it.contentType()?.match(ContentType.Image.PNG) == true
        }
    }
}
