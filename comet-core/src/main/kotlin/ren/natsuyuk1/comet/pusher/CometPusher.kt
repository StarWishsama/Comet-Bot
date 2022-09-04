package ren.natsuyuk1.comet.pusher

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.cometInstances
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

private val logger = KotlinLogging.logger {}

abstract class CometPusher(val name: String, private val defaultConfig: CometPusherConfig) {
    private val configPath = File(resolveDirectory("./config/pusher/"), "$name.yml")
    protected val pendingPushContext = mutableListOf<CometPushContext>()

    var config: CometPusherConfig
        private set

    init {
        runBlocking {
            if (configPath.exists()) {
                config = Yaml.Default.decodeFromString(CometPusherConfig.serializer(), configPath.readTextBuffered())
            } else {
                configPath.touch()
                config = defaultConfig
                configPath.writeTextBuffered(Yaml.Default.encodeToString(config))
            }
        }
    }

    open fun init() {}

    abstract suspend fun retrieve()

    suspend fun push() {
        pendingPushContext.forEach {
            val context = it.normalize()

            it.target.forEach { pt ->
                cometInstances.filter { instance -> instance::class.simpleName?.contains(pt.platform.name.lowercase()) == true }.forEach { comet ->
                    try {
                        when (pt.type) {
                            CometPushTargetType.USER -> {}
                            CometPushTargetType.GROUP -> {
                                comet.getGroup(pt.id)?.sendMessage(context)
                            }
                        }
                    } catch (e: Exception) {
                        logger.warn(e) { "推送 RSS 消息 (${it.id}) 失败" }
                    }
                }
            }
        }

        pendingPushContext.clear()
    }

    open suspend fun stop() {
        configPath.touch()
        configPath.writeTextBuffered(Yaml.Default.encodeToString(config))
    }
}
