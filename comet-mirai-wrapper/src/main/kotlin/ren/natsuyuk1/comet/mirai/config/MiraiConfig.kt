package ren.natsuyuk1.comet.mirai.config

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

val miraiConfigs = mutableListOf<MiraiConfig>()

fun findMiraiConfigByID(id: Long): MiraiConfig? = miraiConfigs.find { it.id == id }

@kotlinx.serialization.Serializable
data class MiraiConfig(
    val id: Long,
    val password: String,
    val protocol: BotConfiguration.MiraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE,
    val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.HeartbeatStrategy.STAT_HB,
) {
    suspend fun init() {
        val configFile = File(configDirectory, "mirai-$id.yml")
        configFile.touch()
        configFile.writeTextBuffered(Yaml.Default.encodeToString(this))
        miraiConfigs.add(this)
    }
}
