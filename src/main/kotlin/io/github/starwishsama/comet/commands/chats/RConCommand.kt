package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.startup.CometRuntime
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.io.IOException

@CometCommand
class RConCommand : ChatCommand, SuspendCommand {
    private val waitList = mutableMapOf<BotUser, Int>()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(user.id) && user.hasPermission(getProps().permission)) {
            if (args.isEmpty()) {
                return getHelp().convertToChain()
            } else {
                when (args[0]) {
                    "setup" -> {
                        SessionManager.addSession(Session(this, user.id))
                        return CometUtil.sendMessage("请在下一条消息发送 rCon 连接地址")
                    }
                    "cmd", "exec", "命令" -> {
                        val rCon = BotVariables.rCon
                        if (rCon != null) {
                            if (args.size > 1) {
                                return try {
                                    withContext(Dispatchers.IO) {
                                        rCon.command(args.getRestString(1)).convertToChain()
                                    }
                                } catch (e: IOException) {
                                    BotVariables.logger.error("在连接到 rCon 服务器时发生了错误", e)
                                    CometUtil.sendMessage("在连接到 rCon 服务器时发生了错误")
                                }
                            }
                        } else {
                            return CometUtil.sendMessage("rCon 还没有设置\n你可以在支持 rCon 的游戏设置下打开 rCon 并设置地址&端口&密码")
                        }
                    }
                    else -> getHelp().convertToChain()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("rcon", arrayListOf("执行命令"), "远程遥控MC服务器", "nbot.commands.rcon", UserLevel.USER)

    override fun getHelp(): String = """
        /rcon setup(设置) 设置 rCon 参数
        /rcon cmd/exec(命令) 遥控MC服务器执行命令
        
        还可以使用 mc, 执行命令 作为等效命令.
    """.trimIndent()

    override fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        if (event.message.contentToString().contains("退出")) {
            waitList.remove(user)
            SessionManager.expireSession(session)
            return
        }

        when (waitList[user] ?: 0) {
            0 -> {
                BotVariables.cfg.rConUrl = event.message.contentToString()
                runBlocking { event.subject.sendMessage(CometUtil.sendMessageAsString("已设置 rCon 连接地址为 ${BotVariables.cfg.rConUrl}\n请在下一条消息发送 rCon 密码\n如果需要退出设置 请回复退出")) }
                waitList[user] = 1
            }
            1 -> {
                val port = event.message.contentToString()
                if (port.isNumeric()) {
                    BotVariables.cfg.rConPort = event.message.contentToString().toInt()
                    runBlocking { event.subject.sendMessage(CometUtil.sendMessage("设置密码成功!\n请在下一条消息发送 rCon 密码\n" +
                            "如果需要退出设置 请回复退出")) }
                    waitList[user] = 2
                } else {
                    runBlocking { event.subject.sendMessage(CometUtil.sendMessage("不是有效的端口\n" +
                            "如果需要退出设置 请回复退出")) }
                }
            }
            2 -> {
                BotVariables.cfg.rConPassword = event.message.contentToString()
                runBlocking { event.subject.sendMessage(CometUtil.sendMessage("设置 rCon 完成!")) }
                CometRuntime.setupRCon()
                waitList.remove(user)
                SessionManager.expireSession(session)
            }
        }
    }
}