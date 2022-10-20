package ren.natsuyuk1.comet.network.thirdparty.arcaea

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveResourceDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered

private val logger = KotlinLogging.logger {}

object ArcaeaHelper {
    internal val songInfo = mutableMapOf<String, String>()
    private val arcaeaFolder = resolveResourceDirectory("arcaea")

    suspend fun load() {
        val song = arcaeaFolder.resolve("arcaea_songs.json")

        if (!song.exists()) {
            song.touch()
            song.writeTextBuffered(json.encodeToString(ArcaeaClient.fetchConstants()))
        } else {
            val modified = song.lastModified()
            val currentTime = System.currentTimeMillis()

            // 15 days
            if (currentTime - modified >= 21600000) {
                val receivedSongInfo = ArcaeaClient.fetchConstants()

                if (songInfo.size != receivedSongInfo.size) {
                    songInfo.clear()
                    songInfo.putAll(receivedSongInfo)
                }
            } else {
                try {
                    songInfo.putAll(json.decodeFromString(song.readTextBuffered()))
                } catch (e: Exception) {
                    logger.warn(e) { "加载 Arcaea 歌曲信息失败, 将无法显示正确的歌曲名称." }
                }
            }
        }
    }

    internal fun getSongNameByID(id: String): String = songInfo[id] ?: id
}
