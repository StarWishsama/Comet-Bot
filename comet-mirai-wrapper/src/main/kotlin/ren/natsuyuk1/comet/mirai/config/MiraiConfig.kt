package ren.natsuyuk1.comet.mirai.config

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.protocol.MiraiLoginProtocol
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

val miraiConfigs = mutableListOf<MiraiConfig>()

fun findMiraiConfigByID(id: Long): MiraiConfig? = miraiConfigs.find { it.id == id }

fun MiraiLoginProtocol.toMiraiProtocol(): BotConfiguration.MiraiProtocol =
    when (this) {
        MiraiLoginProtocol.ANDROID_PHONE -> BotConfiguration.MiraiProtocol.ANDROID_PHONE
        MiraiLoginProtocol.ANDROID_PAD -> BotConfiguration.MiraiProtocol.ANDROID_PAD
        MiraiLoginProtocol.ANDROID_WATCH -> BotConfiguration.MiraiProtocol.ANDROID_WATCH
        MiraiLoginProtocol.IPAD -> BotConfiguration.MiraiProtocol.IPAD
        MiraiLoginProtocol.MACOS -> BotConfiguration.MiraiProtocol.MACOS
    }

@kotlinx.serialization.Serializable
data class MiraiConfig(
    val id: Long,
    val password: String,
    val protocol: MiraiLoginProtocol = MiraiLoginProtocol.ANDROID_PHONE,
) {
    suspend fun init() {
        val configFile = File(configDirectory, "mirai-$id.yml")
        configFile.touch()
        configFile.writeTextBuffered(Yaml.Default.encodeToString(this))
        miraiConfigs.add(this)
    }
}
