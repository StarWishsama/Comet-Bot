package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.commands.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.BotUtil.getRestString
import io.github.starwishsama.comet.utils.isNumeric
import io.github.starwishsama.comet.utils.toMsgChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.io.IOException

class RConCommand : ChatCommand, SuspendCommand {
    private val waitList = mutableMapOf<BotUser, Int>()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.id) && user.hasPermission(getProps().permission)) {
            if (args.isEmpty()) {
                return getHelp().toMsgChain()
            } else {
                when (args[0]) {
                    "setup" -> {
                        SessionManager.addSession(Session(this, user.id))
                        return BotUtil.sendMessage("请在下一条消息发送 rCon 连接地址")
                    }
                    "cmd", "exec", "命令" -> {
                        val rCon = BotVariables.rCon
                        if (rCon != null) {
                            if (args.size > 1) {
                                try {
                                    return withContext(Dispatchers.IO) {
                                        return@withContext rCon.command(args.getRestString(1)).toMsgChain()
                                    }
                                } catch (e: IOException) {
                                    BotVariables.logger.error("在连接到 rCon 服务器时发生了错误", e)
                                    return BotUtil.sendMessage("在连接到 rCon 服务器时发生了错误")
                                }
                            }
                        } else {
                            return BotUtil.sendMessage("rCon 还没有设置\n你可以在支持 rCon 的游戏设置下打开 rCon 并设置地址&端口&密码")
                        }
                    }
                    else -> getHelp().toMsgChain()
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

    override suspend fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        if (event.message.contentToString().contains("退出")) {
            waitList.remove(user)
            SessionManager.expireSession(session)
            return
        }

        when (waitList[user] ?: 0) {
            0 -> {
                BotVariables.cfg.rConUrl = event.message.contentToString()
                event.reply(BotUtil.sendMessageToString("已设置 rCon 连接地址为 ${BotVariables.cfg.rConUrl}\n请在下一条消息发送 rCon 密码\n如果需要退出设置 请回复退出"))
                waitList[user] = 1
            }
            1 -> {
                val port = event.message.contentToString()
                if (port.isNumeric()) {
                    BotVariables.cfg.rConPort = event.message.contentToString().toInt()
                    event.reply(BotUtil.sendMessage("设置密码成功!\n请在下一条消息发送 rCon 密码\n" +
                            "如果需要退出设置 请回复退出"))
                    waitList[user] = 2
                } else {
                    event.reply(BotUtil.sendMessage("不是有效的端口\n" +
                            "如果需要退出设置 请回复退出"))
                }
            }
            2 -> {
                BotVariables.cfg.rConPassword = event.message.contentToString()
                event.reply(BotUtil.sendMessage("设置 rCon 完成!"))
                Comet.setupRCon()
                waitList.remove(user)
                SessionManager.expireSession(session)
            }
        }
    }
}