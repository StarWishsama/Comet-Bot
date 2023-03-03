package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.PJSKMusicDifficultyInfo
import ren.natsuyuk1.comet.util.pjsk.getSekaiBestResourceURL
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.lastModifiedTime
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

object ProjectSekaiMusicDifficulty : ProjectSekaiLocalFile(
    pjskFolder.resolve("musicDifficulties.json"),
    5.days,
) {
    private val musicDifficulties = mutableListOf<PJSKMusicDifficultyInfo>()

    override suspend fun load() {
        try {
            val content = file.readTextBuffered()
            if (content.isBlank()) {
                logger.warn { "加载 Project Sekai 歌曲数据失败, 文件为空" }
            } else {
                musicDifficulties.clear()
                musicDifficulties.addAll(json.decodeFromString(content))
            }
        } catch (e: Exception) {
            logger.warn(e) { "解析歌曲别名数据时出现问题" }
        }
    }

    override suspend fun update(): Boolean {
        file.touch()

        GitHubApi.getSpecificFileCommits("Sekai-World", "sekai-master-db-diff", "musicDifficulties.json")
            .onSuccess {
                val commitTime = Instant.parse(it.first().commit.committer.date)
                val lastModified = file.lastModifiedTime()

                file.touch()

                if (file.length() == 0L || commitTime > lastModified) {
                    if (cometClient.client.downloadFile(
                            getSekaiBestResourceURL(file.name),
                            file,
                        ) == DownloadStatus.OK
                    ) {
                        logger.info { "成功更新歌曲难度数据" }
                        return true
                    }
                }
            }.onFailure {
                logger.warn(it) { "加载 Project Sekai 歌曲难度失败!" }
            }

        return false
    }

    fun getDifficulty(musicId: Int, difficulty: MusicDifficulty) =
        musicDifficulties.find { it.id == musicId && it.musicDifficulty == difficulty }
}
