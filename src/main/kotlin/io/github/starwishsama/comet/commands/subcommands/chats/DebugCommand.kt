package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.youtube.YoutubeApi
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.MessageHandler
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.pojo.youtube.VideoType
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.tasks.HitokotoUpdater
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import java.io.IOException
import kotlin.time.ExperimentalTime

class DebugCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isNotEmpty() && BotUtil.isNoCoolDown(event.sender.id)) {
            when (args[0]) {
                "reload" -> {
                    if (user.isBotOwner()) {
                        return try {
                            DataSetup.reload()
                            BotUtil.sendMsgPrefix("重载成功.").toMsgChain()
                        } catch (e: IOException) {
                            BotUtil.sendMsgPrefix("在重载时发生了异常.").toMsgChain()
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
                            var i = 1
                            for (session in sessions) {
                                sb.append(i + 1).append(" ").append(session.key.toString()).append("\n")
                                i++
                            }
                        }
                        return sb.toString().trim().toMsgChain()
                    }
                }
                "help" -> return getHelp().toMessage().asMessageChain()
                "info" ->
                    return ("彗星 Bot ${BotVariables.version}\n" +
                            "今日もかわいい~\n" +
                            "已注册命令数: ${MessageHandler.countCommands()}\n" +
                            BotUtil.getMemoryUsage()).toMsgChain()
                "hitokoto" -> return HitokotoUpdater.getHitokoto().toMsgChain()
                "switch" -> {
                    BotVariables.switch = !BotVariables.switch

                    return if (!BotVariables.switch) {
                        BotUtil.sendMsgPrefix("おつまち~").toMsgChain()
                    } else {
                        BotUtil.sendMsgPrefix("今日もかわいい!").toMsgChain()
                    }
                }
                "youtube" -> {
                    if (args.size > 1) {
                        val result = YoutubeApi.getChannelVideos(args[1], 10)
                        result?.items?.forEach {
                            if (it.snippet.getType() == VideoType.STREAMING) {
                                return """
                                    ${it.snippet.videoTitle}
                                    ${it.snippet.desc}
                                    ${it.snippet.publishTime}
                                """.trimIndent().toMsgChain()
                            }
                        }
                    }
                }
                "push" -> {
                    if (event is GroupMessageEvent) {
                        val group = GroupConfigManager.getConfigSafely(event.group.id)
                        group.twitterPushEnabled = true
                    }
                }
                else -> return "Bot > 命令不存在\n${getHelp()}".toMsgChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("debug", null, "Debug", "nbot.commands.debug", UserLevel.ADMIN)
    }

    override fun getHelp(): String = "直接开 IDE 看会死掉吗"
}
