/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.service.task.HitokotoUpdater
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.RuntimeUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.io.IOException
import java.util.concurrent.ThreadPoolExecutor
import kotlin.time.ExperimentalTime


class DebugCommand : ChatCommand, UnDisableableCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "reload" -> {
                    if (user.isBotOwner()) {
                        return try {
                            DataSetup.reload()
                            "重载成功.".toChain()
                        } catch (e: IOException) {
                            "在重载时发生了异常.".toChain()
                        }
                    }
                }
                "session" -> {
                    if (user.isBotOwner()) {
                        val sb = StringBuilder("目前活跃的会话列表: \n").apply {
                            val sessions = SessionHandler.getSessions()

                            if (sessions.isEmpty()) {
                                append("无")
                            } else {
                                for ((i, session) in sessions.withIndex()) {
                                    append(i + 1).append(" ").append(session.toString()).append("\n")
                                }
                            }
                        }
                        return sb.toString().trim().toChain()
                    }
                }
                "info" -> {
                    if (args.size == 1) {
                        val ping = try {
                            NetUtil.checkPingValue()
                        } catch (t: IOException) {
                            -1L
                        }
                        return ("彗星 Bot ${BuildConfig.version}\n" +
                                "已注册命令数: ${CommandManager.countCommands()}\n" +
                                "与服务器的延迟为 $ping ms\n" +
                                "运行时长 ${CometUtil.getRunningTime()}\n" +
                                "构建时间: ${BuildConfig.buildTime}"
                                ).toChain()
                    } else {
                        return when (args[1]) {
                            "memory", "ram" -> {
                                RuntimeUtil.getMemoryInfo().toChain(false)
                            }
                            "thread", "thr" -> {
                                val executor = TaskUtil.service as ThreadPoolExecutor

                                val queueSize: Int = executor.queue.size
                                val activeCount: Int = executor.activeCount
                                val completedTaskCount: Long = executor.completedTaskCount
                                val taskCount: Long = executor.taskCount

                                return """
当前队列中线程数：$queueSize
当前活动线程数：$activeCount
执行完成线程数： $completedTaskCount
总线程数：$taskCount
                    """.trimIndent().convertToChain()
                            }
                            else -> "无效参数".toChain()
                        }
                    }
                }
                "hitokoto" -> return HitokotoUpdater.getHitokoto().convertToChain()
                "switch" -> {
                    CometVariables.switch = !CometVariables.switch
                    return "维护模式已${if (!CometVariables.switch) "开启" else "关闭"}".toChain()
                }
                "push" -> {
                    if (args.size > 1) {
                        return when (args[1].lowercase()) {
                            "twit", "twitter", "推特", "蓝鸟", "twi" -> {
                                PusherManager.getPusherByName("data")?.execute()
                                toChain("Twitter retriever has been triggered and run~")
                            }
                            "bilibili", "bili", "哔哩哔哩", "b站" -> {
                                PusherManager.getPusherByName("bili_dynamic")?.execute()
                                    ?: return "Can't found pusher".toChain()
                                toChain("Bilibili retriever has been triggered and run~")
                            }
                            "status" -> {
                                val ps = PusherManager.getPushers()
                                buildString {
                                    ps.forEach {
                                        append(it::class.java.simpleName + "\n")
                                        append("上次推送了 ${it.pushTime} 次\n")
                                        append("上次推送于 ${CometVariables.yyMMddPattern.format(it.latestTriggerTime)}\n")
                                    }
                                    trim()
                                }.toChain()
                            }
                            else -> toChain("Unknown retriever type.")
                        }
                    }
                }
                "quit" -> {
                    if (event is GroupMessageEvent) {
                        event.group.quit()
                    }
                }
                else -> return "Bot > 命令不存在\n${getHelp()}".convertToChain()
            }
        }
        return EmptyMessageChain
    }

    override var props: CommandProps =
        CommandProps("debug", mutableListOf(), "Debug", "nbot.commands.debug", UserLevel.ADMIN)


    override fun getHelp(): String = "调试命令会随时变动, 请自行查阅代码"

    override val isHidden: Boolean
        get() = true

    override val canRegister: () -> Boolean
        get() = { CometVariables.cfg.debugMode }
}