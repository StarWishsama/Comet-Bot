package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.commands.interfaces.WaitableCommand
import io.github.starwishsama.nbot.enums.SessionType
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.Session
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.isNumeric
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.io.IOException

class RConCommand : UniversalCommand, WaitableCommand {
    private val waitList = mutableMapOf<BotUser, Int>()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ) && user.hasPermission(getProps().permission)) {
            if (args.isEmpty()) {
                return getHelp().toMirai()
            } else {
                when (args[0]) {
                    "setup" -> {
                        SessionManager.addSession(Session(SessionType.DELAY, this, user.userQQ))
                        return BotUtil.sendMsgPrefix("请在下一条消息发送 rCon 连接地址").toMirai()
                    }
                    "cmd", "exec", "命令" -> {
                        val rCon = BotInstance.rCon
                        if (rCon != null) {
                            if (args.size > 1) {
                                try {
                                    return withContext(Dispatchers.IO) {
                                        return@withContext rCon.command(BotUtil.getRestStringInArgs(args, 1)).toMirai()
                                    }
                                } catch (e: IOException) {
                                    BotInstance.logger.error("在连接到 rCon 服务器时发生了错误", e)
                                    return BotUtil.sendMsgPrefix("在连接到 rCon 服务器时发生了错误").toMirai()
                                }
                            }
                        } else {
                            return BotUtil.sendMsgPrefix("rCon 还没有设置\n你可以在 MC 服务端设置下打开 rCon 并设置地址/端口/密码").toMirai()
                        }
                    }
                    else -> getHelp().toMirai()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("rcon", arrayListOf("执行命令", "mc"), "远程遥控MC服务器", "nbot.commands.rcon", UserLevel.USER)

    override fun getHelp(): String = """
        /rcon setup(设置) 设置 rCon 参数
        /rcon cmd/exec(命令) 遥控MC服务器执行命令
        
        还可以使用 mc, 执行命令 作为等效命令.
    """.trimIndent()

    override suspend fun replyResult(message: MessageEvent, user: BotUser, session: Session) {
        if (message.message.contentToString().contains("退出")) {
            waitList.remove(user)
            SessionManager.expireSession(session)
            return
        }

        when (waitList[user] ?: 0) {
            0 -> {
                BotConstants.cfg.rConUrl = message.message.contentToString()
                message.reply(BotUtil.sendMsgPrefix("已设置 rCon 连接地址为 ${BotConstants.cfg.rConUrl}\n请在下一条消息发送 rCon 密码\n如果需要退出设置 请回复退出"))
                waitList[user] = 1
            }
            1 -> {
                val port = message.message.contentToString()
                if (port.isNumeric()) {
                    BotConstants.cfg.rConPort = message.message.contentToString().toInt()
                    message.reply(BotUtil.sendMsgPrefix("设置密码成功!\n请在下一条消息发送 rCon 密码\n" +
                            "如果需要退出设置 请回复退出"))
                    waitList[user] = 2
                } else {
                    message.reply(BotUtil.sendMsgPrefix("不是有效的端口\n" +
                            "如果需要退出设置 请回复退出"))
                }
            }
            2 -> {
                BotConstants.cfg.rConPassword = message.message.contentToString()
                message.reply(BotUtil.sendMsgPrefix("设置 rCon 完成!"))
                BotInstance.setupRCon()
                waitList.remove(user)
                SessionManager.expireSession(session)
            }
        }
    }
}