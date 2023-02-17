package ren.natsuyuk1.comet.objects.pjsk.local

import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiMusicInfo
import ren.natsuyuk1.comet.service.RateLimitAPI
import ren.natsuyuk1.comet.service.RateLimitService
import ren.natsuyuk1.comet.util.pjsk.getSekaiBestResourceURL
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.*
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
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
    1.days,
) {
    private val musicCoverFolder = pjskFolder.resolve("covers/")

    internal val musicDatabase = mutableMapOf<Int, ProjectSekaiMusicInfo>()

    override suspend fun load() {
        musicDatabase.clear()

        try {
            val content = file.readTextBuffered()
            if (content.isBlank()) {
                logger.warn { "加载 Project Sekai 歌曲数据失败, 文件为空" }
            } else {
                val music = json.decodeFromString(
                    ListSerializer(ProjectSekaiMusicInfo.serializer()),
                    content,
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
        if (RateLimitService.isRateLimit(RateLimitAPI.GITHUB)) {
            return false
        }

        GitHubApi.getSpecificFileCommits("Sekai-World", "sekai-master-db-diff", file.name).onSuccess {
            val current = file.lastModifiedTime()
            val lastUpdate = Instant.parse(it.first().commit.committer.date)

            if (!file.exists() || lastUpdate > current) {
                file.touch()
                if (cometClient.client.downloadFile(
                        getSekaiBestResourceURL(file.name),
                        file,
                    ) == DownloadStatus.OK
                ) {
                    logger.info { "成功更新音乐数据" }
                    return true
                }
            }
        }

        return false
    }

    fun getMusicInfo(name: String): ProjectSekaiMusicInfo? = musicDatabase.values.find { it.title == name }

    fun getMusicInfo(id: Int): ProjectSekaiMusicInfo? = musicDatabase[id]

    fun fuzzyGetMusicInfo(name: String, minSimilarity: Double = 0.35): Pair<ProjectSekaiMusicInfo?, BigDecimal> {
        val normalizeName = name.normalize(Normalizer.Form.NFKC)

        return musicDatabase.values.associateWith {
            val alias = ProjectSekaiMusicAlias.getAlias(it.id) ?: emptyArray()
            val sim = ldSimilarity(it.title.normalize(Normalizer.Form.NFKC), normalizeName)
                .max(
                    alias.maxOfOrNull { a -> ldSimilarity(a.normalize(Normalizer.Form.NFKC), normalizeName) }
                        ?: BigDecimal.ZERO,
                )

            sim
        }.filter { it.value > BigDecimal.valueOf(minSimilarity) }
            .maxByOrNull { it.value }.let {
                Pair(it?.key, it?.value ?: BigDecimal.ZERO)
            }
    }

    suspend fun getMusicCover(music: ProjectSekaiMusicInfo): File {
        if (!musicCoverFolder.exists()) {
            musicCoverFolder.mkdir()
        }

        val cover = musicCoverFolder.resolve(music.assetBundleName + ".png")

        if (cover.isBlank() || !cover.isType("image/png")) {
            downloadMusicCover(music)
        }

        return cover
    }

    private suspend fun downloadMusicCover(music: ProjectSekaiMusicInfo) {
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
