package ren.natsuyuk1.comet.mirai.config

import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.secondsToMillis
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

private val logger = KotlinLogging.logger {}

object MiraiConfigManager {
    private val configFile = File(configDirectory, "./mirai")
    private val miraiConfigs = mutableListOf<MiraiConfig>()
    private var isInit = false

    suspend fun init() {
        if (isInit) return

        if (!configFile.exists()) {
            configFile.mkdir()
        }

        configFile.listFiles()?.forEach {
            try {
                registerConfig(Yaml.Default.decodeFromString(MiraiConfig.serializer(), it.readTextBuffered()))
            } catch (e: Exception) {
                if (e is SerializationException) {
                    logger.warn(e) { "在解析 ${it.name} 时出现问题" }
                }
            }
        }

        isInit = true
    }

    fun registerConfig(cfg: MiraiConfig) = miraiConfigs.add(cfg)

    fun findMiraiConfigByID(id: Long): MiraiConfig? = miraiConfigs.find { it.id == id }
}

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
    @Comment("Mirai 登录协议")
    val protocol: MiraiLoginProtocol = MiraiLoginProtocol.ANDROID_PHONE,
    @Comment("Mirai 心跳策略, 一般情况下无需改动")
    val heartbeatStrategy: BotConfiguration.HeartbeatStrategy = BotConfiguration.HeartbeatStrategy.STAT_HB,
    @Comment("Mirai 心跳周期, 单位为毫秒")
    val heartbeatPeriodMillis: Long = 60.secondsToMillis,
) {
    suspend fun init() {
        val configFile = File(configDirectory, "mirai-config-$id.yml")

        configFile.touch()
        configFile.writeTextBuffered(Yaml.Default.encodeToString(this))

        MiraiConfigManager.registerConfig(this)
    }
}
