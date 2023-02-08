package ren.natsuyuk1.comet.objects.pjsk.local

import io.ktor.http.*
import kotlinx.serialization.builtins.ListSerializer
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProfileMusicInfo
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.isBlank
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.DownloadStatus
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

object PJSKProfileMusic : ProjectSekaiLocalFile(
    pjskFolder.resolve("musics_pjsekai.json"),
    2.days
) {
    private const val url = "https://musics.pjsekai.moe/musics.json"

    private val musicDatabase = mutableMapOf<Int, ProfileMusicInfo>()

    override suspend fun load() {
        musicDatabase.clear()

        try {
            val content = file.readTextBuffered()
            if (content.isBlank()) {
                logger.warn { "加载 Project Sekai 歌曲数据失败, 文件为空" }
            } else {
                val music = json.decodeFromString(
                    ListSerializer(ProfileMusicInfo.serializer()),
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

        if (file.isBlank() || isOutdated()) {
            if (cometClient.client.downloadFile(url, file) == DownloadStatus.OK) {
                logger.info { "成功更新音乐数据" }
                return true
            }
        }

        return false
    }

    fun getMusicInfo(id: Int): ProfileMusicInfo? = musicDatabase[id]
}
