package ren.natsuyuk1.comet.pusher

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mu.KotlinLogging
import net.mamoe.yamlkt.Yaml
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.cometInstances
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

abstract class CometPusher(val name: String, private val defaultConfig: CometPusherConfig) {
    private var job: Job? = null
    private val configPath = File(resolveDirectory("./config/pusher/"), "$name.yml")
    private var nightTime: Pair<Int, Int>

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

            val duration = config.nightModeDuration.split("-")

            if (duration.size != 2 || duration.any { it.toIntOrNull() == null || it.toInt() !in 0..23 }) {
                logger.warn("推送器 $name 夜间模式间隔有误! 已重置为默认值.")
                config.nightModeDuration = "0-6"
            }

            nightTime = Pair(duration[0].toInt(), duration[1].toInt())
        }
    }

    open fun init() {
        job = TaskManager.registerTask({
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val (start, end) = nightTime
            if (now.hour in start..end) {
                delay(config.nightPushInterval.toDuration(config.pushIntervalUnit))
            } else {
                delay(config.pushInterval.toDuration(config.pushIntervalUnit))
            }
        }) {
            retrieve()
            push()
        }

        transaction {
            CometPusherContext.deleteOutdatedContext(name)
        }
    }

    internal abstract suspend fun retrieve()

    internal suspend fun push() {
        var counter = 0

        pendingPushContext.forEach {
            val context = it.normalize()

            it.target.forEach { pt ->
                cometInstances.filter { instance -> pt.platform == instance.platform }.forEach { comet ->
                    try {
                        when (pt.type) {
                            CometPushTargetType.USER -> {}
                            CometPushTargetType.GROUP -> {
                                comet.getGroup(pt.id)?.sendMessage(context)
                                counter++
                            }
                        }
                    } catch (e: Exception) {
                        logger.warn(e) { "推送 RSS 消息 (${it.id}) 失败" }
                    }
                }
            }
        }

        pendingPushContext.clear()

        logger.debug { "推送器 \"$name\" 已推送 $counter 条推送内容." }
    }

    open suspend fun stop() {
        configPath.touch()
        configPath.writeTextBuffered(Yaml.Default.encodeToString(config))
        job?.cancel()
    }
}
