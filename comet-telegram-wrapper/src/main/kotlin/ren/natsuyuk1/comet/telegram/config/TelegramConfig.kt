package ren.natsuyuk1.comet.telegram.config

import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

val telegramConfigs = mutableListOf<TelegramConfig>()

fun findTelegramConfigByID(token: String): TelegramConfig? = telegramConfigs.find { it.token == token }

@kotlinx.serialization.Serializable
data class TelegramConfig(
    val id: Long,
    val token: String
) {
    suspend fun init() {
        val configFile = File(configDirectory, "telegram-${id}.yml")
        configFile.touch()
        configFile.writeTextBuffered(Yaml.Default.encodeToString(this))
        telegramConfigs.add(this)
    }
}
