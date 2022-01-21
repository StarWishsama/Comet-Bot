/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.service.pusher

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.createBackupFile
import io.github.starwishsama.comet.utils.writeClassToJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.Instant
import java.util.concurrent.ScheduledFuture

abstract class CometPusher(
    val name: String,
    private val defaultData: CometPusherData
) {
    lateinit var data: CometPusherData
    lateinit var task: ScheduledFuture<*>
    var lastTriggerTime: Instant = Instant.now()
    var pushTime: Int = 0

    private val dataFile = File(FileUtil.getChildFolder("pushers"), "$name.json")

    abstract fun retrieve()

    fun push() {
        println(data.cache)

        data.cache.forEach { context ->
            if (context.status == PushStatus.PENDING) {
                context.status = PushStatus.PROGRESSING

                val wrapper = context.toMessageWrapper()

                daemonLogger.debug("正在尝试推送消息 ${wrapper::class.simpleName}#${wrapper.hashCode()}, 可用状态: ${wrapper.isUsable()}")

                context.pushTarget.forEach group@{

                    if (!comet::isInitialized.invoke()) {
                        return@group
                    }

                    try {
                        if (wrapper.isUsable()) {
                            val group = comet.getBot().getGroup(it) ?: return@group

                            runBlocking {
                                group.sendMessage(wrapper.toMessageChain(group))
                                delay(RandomUtil.randomLong(1000, 2000))
                            }

                            pushTime++
                        } else {
                            context.status = PushStatus.FAILED
                            return@group
                        }
                    } catch (e: Exception) {
                        daemonLogger.warning("在推送消息至群 $it 时出现异常", e)
                        context.status = PushStatus.FAILED
                        return@group
                    }
                }

                context.status = PushStatus.DONE

            }
        }

        if (pushTime > 0) {
            daemonLogger.verbose("$name 已成功推送 $pushTime 个消息")
            pushTime = 0
        }

        lastTriggerTime = Instant.now()
        saveData()
    }

    fun start() {
        if (!dataFile.exists()) {
            dataFile.createNewFile()
            data = defaultData
            dataFile.writeClassToJson(defaultData)
        } else {
            kotlin.runCatching {
                data = mapper.readValue(dataFile, CometPusherData::class.java)
            }.onSuccess {
                daemonLogger.log(HinaLogLevel.Info, "加载 $name 数据成功", prefix = "推送器")
            }.onFailure {
                daemonLogger.log(HinaLogLevel.Warn, "加载 $name 数据时出现异常", throwable = it, prefix = "推送器")
                dataFile.createBackupFile()
                data = defaultData
                dataFile.writeClassToJson(data)
            }
        }

        task = TaskUtil.scheduleAtFixedRate(data.interval, data.interval, data.timeUnit) {
            retrieve()
            push()
        }
    }

    private fun saveData() {
        dataFile.writeClassToJson(data)
    }

    fun stop() {
        task.cancel(true)
        saveData()
    }
}