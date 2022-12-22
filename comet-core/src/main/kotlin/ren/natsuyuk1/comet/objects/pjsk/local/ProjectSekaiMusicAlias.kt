package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.util.pjsk.getCometDatabaseURL
import ren.natsuyuk1.comet.util.pjsk.pjskFolder
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.ktor.downloadFile
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger {}

object ProjectSekaiMusicAlias : ProjectSekaiLocalFile(
    pjskFolder.resolve("music_title.json"),
    5.days
) {
    private val url = getCometDatabaseURL("music_title.json")
    private var musicAliasDatabase = mapOf<Int, Array<String>>()

    override suspend fun load() {
        try {
            val content = file.readTextBuffered()
            if (content.isBlank()) {
                logger.warn { "加载 Project Sekai 歌曲数据失败, 文件为空" }
            } else {
                musicAliasDatabase = json.decodeFromString(
                    MapSerializer(Int.serializer(), ArraySerializer(String.serializer())),
                    content
                )
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

    fun getAlias(musicId: Int) = musicAliasDatabase[musicId]
}
