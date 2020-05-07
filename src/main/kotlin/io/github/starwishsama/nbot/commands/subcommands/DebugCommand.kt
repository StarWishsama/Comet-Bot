package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.exceptions.RateLimitException
import io.github.starwishsama.nbot.file.DataSetup
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.getRestString
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import io.github.starwishsama.nbot.util.TwitterUtil
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage
import java.io.IOException

class DebugCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isNotEmpty() && BotUtil.isNoCoolDown(event.sender.id)) {
            when (args[0]) {
                "image" -> {
                    return BotUtil.getImageStream("https://i.loli.net/2020/04/14/INmrZVhiyK5dgbk.jpg")
                        .uploadAsImage(event.subject).asMessageChain()
                }
                "twitter" -> {
                    return if (args.size == 1) {
                        BotUtil.sendMsgPrefix("/debug twitter [蓝鸟账号]").toMirai()
                    } else {
                        try {
                            val twitterUser = TwitterUtil.getUserInfo(args.getRestString(1))
                            if (twitterUser == null) {
                                BotUtil.sendMsgPrefix("找不到此用户或连接超时").toMirai()
                            } else {
                                BotUtil.sendMsgPrefix(
                                    "\n${twitterUser.name}\n" +
                                            "粉丝数: ${twitterUser.followersCount}\n" +
                                            "最近推文: \n" +
                                            TwitterUtil.getLatestTweet(args.getRestString(1))?.text
                                ).toMirai()
                            }
                        } catch (e: RateLimitException) {
                            BotUtil.sendMsgPrefix("API 调用已达上限").toMirai()
                        }
                    }
                }
                "reload" -> {
                    if (user.isBotOwner()) {
                        return try {
                            DataSetup.reload()
                            BotUtil.sendMsgPrefix("重载成功.").toMirai()
                        } catch (e: IOException) {
                            BotUtil.sendMsgPrefix("在重载时发生了异常.").toMirai()
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
                            for (i in sessions.indices) {
                                sb.append(i + 1).append(" ").append(sessions[i].toString()).append("\n")
                            }
                        }
                        return sb.toString().trim().toMirai()
                    }
                }
                "help" -> return getHelp().toMessage().asMessageChain()
                else -> return "Bot > 命令不存在\n${getHelp()}".toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("debug", null, "nbot.commands.debug", "", UserLevel.ADMIN)
    }

    override fun getHelp(): String = "直接开 IDE 看会死掉吗"
}
