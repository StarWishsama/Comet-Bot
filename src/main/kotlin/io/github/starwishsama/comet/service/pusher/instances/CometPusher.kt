/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.instances

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.PushContext
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.writeClassToJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import java.io.File
import java.time.LocalDateTime

/**
 * [CometPusher]
 *
 * Comet 推送器的抽象类
 * 可按需实现获取和推送, Comet 会按照配置依次推送
 */
abstract class CometPusher(
    val bot: Bot,
    val name: String,
    var config: PusherConfig = PusherConfig(0)
) {
    var retrieveTime: Int = 0

    var pushTime: Int = 0

    val cachePool = mutableSetOf<PushContext>()

    var latestTriggerTime: LocalDateTime = LocalDateTime.now()

    /**
     * 获取推送数据方法
     */
    abstract fun retrieve()

    /**
     * 推送方法
     */
    fun push() {
        cachePool.forEach { context ->
            if (context.status == PushStatus.CREATED || context.status == PushStatus.PROGRESSING) {

                context.status = PushStatus.PUSHING

                val wrapper = context.toMessageWrapper()

                daemonLogger.debug("正在尝试推送消息 #${wrapper.hashCode()}, 可用状态: ${wrapper.isUsable()}")

                context.getPushTarget().forEach group@{
                    try {
                        if (wrapper.isUsable()) {
                            val group = bot.getGroup(it) ?: return@group

                            runBlocking {
                                group.sendMessage(wrapper.toMessageChain(group))
                                delay(RandomUtil.randomLong(1000, 2000))
                            }

                            pushTime++
                        } else {
                            context.status = PushStatus.INVAILD
                        }
                    } catch (e: Exception) {
                        daemonLogger.warning("在推送消息至群 $it 时出现异常", e)
                    }
                }

                context.status = PushStatus.PUSHED
            }
        }

        if (pushTime > 0) {
            daemonLogger.verbose("$name 已成功推送 $pushTime 个消息")
            pushTime = 0
        }

        latestTriggerTime = LocalDateTime.now()
        save()
    }


    fun save() {
        val cfgFile = File(PusherManager.pusherFolder, "${name}.json")

        if (!cfgFile.exists()) cfgFile.createNewFile()

        config.cachePool.addAll(cachePool)

        cfgFile.writeClassToJson(config)
    }

    fun start() {
        if (config.cachePool.isNotEmpty()) {
            try {
                cachePool.addAll(config.cachePool)
            } catch (e: Exception) {
                daemonLogger.warning("无法解析 $name 历史推送记录")
            }
        }

        TaskUtil.runScheduleTaskAsync(config.interval, config.interval, config.timeUnit) {
            execute()
        }

        daemonLogger.info("$name 推送器已启动, 载入缓存 ${cachePool.size} 个")
    }

    fun execute() {
        retrieve()
        push()
    }
}