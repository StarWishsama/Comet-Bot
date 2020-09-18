package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.annotations.CometCommand
import io.github.starwishsama.comet.api.youtube.YoutubeApi
import io.github.starwishsama.comet.commands.CommandExecutor
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.pushers.HitokotoUpdater
import io.github.starwishsama.comet.pushers.TweetUpdateChecker
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.network.RssUtil
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.uploadAsGroupVoice
import java.io.File
import java.io.IOException
import java.util.concurrent.ThreadPoolExecutor
import kotlin.time.ExperimentalTime

@CometCommand
class DebugCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isNotEmpty() && BotUtil.hasNoCoolDown(event.sender.id)) {
            when (args[0]) {
                "reload" -> {
                    if (user.isBotOwner()) {
                        return try {
                            DataSetup.reload()
                            BotUtil.sendMessage("重载成功.")
                        } catch (e: IOException) {
                            BotUtil.sendMessage("在重载时发生了异常.")
                        }
                    }
                }
                "session" -> {
                    if (user.isBotOwner()) {
                        val sb = StringBuilder("目前活跃的会话列表: \n")
                        val sessions = SessionManager.getSessions()
                        if (sessions.isEmpty()) {
                            sb.append("无")
                        } else {
                            var i = 0
                            for (session in sessions) {
                                sb.append(i + 1).append(" ").append(session.key.toString()).append("\n")
                                i++
                            }
                        }
                        return sb.toString().trim().convertToChain()
                    }
                }
                "help" -> return PlainText(getHelp()).asMessageChain()
                "info" ->
                    return ("彗星 Bot ${BotVariables.version}\n" +
                            "今日もかわいい~\n" +
                            "已注册命令数: ${CommandExecutor.countCommands()}\n" +
                            BotUtil.getMemoryUsage() + "\n" +
                            "与服务器的延迟为 ${NetUtil.checkPingValue()} ms"
                            ).convertToChain()
                "hitokoto" -> return HitokotoUpdater.getHitokoto().convertToChain()
                "switch" -> {
                    BotVariables.switch = !BotVariables.switch

                    return if (!BotVariables.switch) {
                        BotUtil.sendMessage("おつまち~")
                    } else {
                        BotUtil.sendMessage("今日もかわいい!")
                    }
                }
                "tpush" -> {
                    TweetUpdateChecker.retrieve()
                    return BotUtil.sendMessage("Tweet retriever has been triggered and run~")
                }
                "youtube" -> {
                    if (args.size > 1) {
                        val result = YoutubeApi.getChannelVideos(args[1], 10)
                        return YoutubeApi.getLiveStatusByResult(result).toMessageChain(event.subject)
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
                "watame" -> {
                    try {
                        val voice = File(
                            FileUtil.getResourceFolder().toString() + "${File.separator}voice",
                            "watame_janken.amr"
                        ).inputStream()
                        if (event is GroupMessageEvent)
                            return voice.uploadAsGroupVoice(event.group).asMessageChain()
                    } catch (ignored: Throwable) {
                        return "File doesn't exists.".convertToChain()
                    }
                }
                "executors" -> {
                    val executor = BotVariables.service as ThreadPoolExecutor

                    val queueSize: Int = executor.queue.size
                    val activeCount: Int = executor.activeCount
                    val completedTaskCount: Long = executor.completedTaskCount
                    val taskCount: Long = executor.taskCount

                    return """
                        当前排队线程数：$queueSize
                        当前活动线程数：$activeCount
                        执行完成线程数： $completedTaskCount
                        总线程数：$taskCount
                    """.trimIndent().convertToChain()
                }
                else -> return "Bot > 命令不存在\n${getHelp()}".convertToChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("debug", null, "Debug", "nbot.commands.debug", UserLevel.ADMIN)
    }

    override fun getHelp(): String = "Debug 中的命令会随时变动, 请自行查阅代码"
}