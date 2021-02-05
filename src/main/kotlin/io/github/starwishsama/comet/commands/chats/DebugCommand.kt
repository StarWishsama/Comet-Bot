package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.service.task.HitokotoUpdater
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.network.RssUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.io.IOException
import java.util.concurrent.ThreadPoolExecutor
import kotlin.time.ExperimentalTime

@CometCommand
class DebugCommand : ChatCommand, UnDisableableCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isNotEmpty() && CometUtil.isNoCoolDown(event.sender.id)) {
            when (args[0]) {
                "reload" -> {
                    if (user.isBotOwner()) {
                        return try {
                            DataSetup.reload()
                            "重载成功.".sendMessage()
                        } catch (e: IOException) {
                            "在重载时发生了异常.".sendMessage()
                        }
                    }
                }
                "session" -> {
                    if (user.isBotOwner()) {
                        val sb = StringBuilder("目前活跃的会话列表: \n").apply {
                            val sessions = SessionManager.getSessions()

                            if (sessions.isEmpty()) {
                                append("无")
                            } else {
                                for ((i, session) in sessions.withIndex()) {
                                    append(i + 1).append(" ").append(session.toString()).append("\n")
                                }
                            }
                        }
                        return sb.toString().trim().sendMessage()
                    }
                }
                "info" -> {
                    val ping = try {
                        NetUtil.checkPingValue()
                    } catch (t: IOException) {
                        -1L
                    }
                    return ("彗星 Bot ${BuildConfig.version}\n" +
                            "今日もかわいい~\n" +
                            "已注册命令数: ${CommandExecutor.countCommands()}\n" +
                            CometUtil.getMemoryUsage() + "\n" +
                            "与服务器的延迟为 $ping ms\n" +
                            "构建时间: ${BuildConfig.buildTime}"
                            ).sendMessage()
                }
                "hitokoto" -> return HitokotoUpdater.getHitokoto().convertToChain()
                "switch" -> {
                    BotVariables.switch = !BotVariables.switch
                    return "维护模式已${if (!BotVariables.switch) "开启" else "关闭"}".sendMessage()
                }
                "push" -> {
                    if (args.size > 1) {
                        return when (args[1].toLowerCase()) {
                            "twit", "twitter", "推特", "蓝鸟", "twi" -> {
                                PusherManager.getPusherByName("twitter")?.execute()
                                sendMessage("Twitter retriever has been triggered and run~")
                            }
                            "ytb", "y2b", "youtube", "油管" -> {
                                sendMessage("Youtube retriever is in WIP status.")
                            }
                            "bilibili", "bili", "哔哩哔哩", "b站" -> {
                                PusherManager.getPusherByName("bili_dynamic")?.execute() ?: return "Can't found pusher".sendMessage()
                                sendMessage("Bilibili retriever has been triggered and run~")
                            }
                            "status" -> {
                                val ps = PusherManager.getPushers()
                                buildString {
                                    ps.forEach {
                                        append(it::class.java.simpleName + "\n")
                                        append("上次推送了 ${it.pushTime} 次\n")
                                        append("上次推送于 ${BotVariables.yyMMddPattern.format(it.latestPushTime)}\n")
                                    }
                                    trim()
                                }.sendMessage()
                            }
                            else -> sendMessage("Unknown retriever type.")
                        }
                    }
                }
                "youtube" -> {
                    if (args.size > 1) {
                        return YoutubeApi.getLiveStatusByResult(YoutubeApi.getChannelVideos(args[1], 10)).toMessageChain(event.subject)
                    }
                }
                "rss" -> {
                    if (args.size > 1) {
                        return RssUtil.simplifyHTML(
                                RssUtil.getFromEntry(
                                        RssUtil.getEntryFromURL(args[1])
                                                ?: return "Can't retrieve page content".convertToChain()
                                )
                        ).convertToChain()
                    }
                }
                "executors" -> {
                    val executor = BotVariables.service as ThreadPoolExecutor

                    val queueSize: Int = executor.queue.size
                    val activeCount: Int = executor.activeCount
                    val completedTaskCount: Long = executor.completedTaskCount
                    val taskCount: Long = executor.taskCount

                    val tasks = buildString {
                        append("任务列表:\n")
                        executor.queue.forEach {
                            append((it.hashCode())).append(" ")
                        }
                    }.trim()

                    return """
当前排队线程数：$queueSize
当前活动线程数：$activeCount
执行完成线程数： $completedTaskCount
总线程数：$taskCount
$tasks
                    """.trimIndent().convertToChain()
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

    override fun getProps(): CommandProps {
        return CommandProps("debug", mutableListOf(), "Debug", "nbot.commands.debug", UserLevel.ADMIN)
    }

    override fun getHelp(): String = "调试命令会随时变动, 请自行查阅代码"

    override val isHidden: Boolean
        get() = true

    override val canRegister: () -> Boolean
        get() = { BotVariables.cfg.debugMode }
}