package io.github.starwishsama.comet.service.pusher

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.service.pusher.config.EmptyPusherConfig
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.PushContext
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * [CometPusher]
 */
abstract class CometPusher(val bot: Bot, val name: String) {
    open var config: PusherConfig = EmptyPusherConfig()

    abstract val cachePool: MutableList<PushContext>

    var retrieveTime: Int = 0

    var pushTime: Int = 0

    var latestPushTime: LocalDateTime = LocalDateTime.now()

    abstract fun retrieve()

    open fun push() {
        cachePool.forEach { context ->
            if (context.status == PushStatus.READY) {
                context.getPushTarget().forEach group@{
                    try {
                        val group = bot.getGroup(it) ?: return@group

                        runBlocking {
                            group.sendMessage(context.toMessageWrapper().toMessageChain(group))
                            delay(RandomUtil.randomLong(1000, 2000))
                        }

                        addPushTime()
                    } catch (e: Exception) {
                        daemonLogger.warning("在推送开播消息至群 $it 时出现异常", e)
                    }
                }

                context.clearPushTarget()
                context.status = PushStatus.FINISHED
            }
        }

        if (pushTime > 0) {
            daemonLogger.verboseS("$name 已成功推送 $pushTime 个消息")
            resetPushTime()
        }

        latestPushTime = LocalDateTime.now()
    }

    fun execute() {
        retrieve()
        push()
    }

    fun start() {
        TaskUtil.runScheduleTaskAsync(config.interval, config.interval, TimeUnit.MILLISECONDS) {
            execute()
        }
        daemonLogger.info("$name 推送器已启动")
    }

    fun addPushTime(){
        pushTime += 1
    }

    fun resetPushTime() {
        pushTime = 0
    }

    fun addRetrieveTime() {
        retrieveTime += 1
    }

    fun resetRetrieveTime() {
        retrieveTime = 0
    }
}