package io.github.starwishsama.nbot.listeners

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.interfaces.WaitableCommand
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.Session
import io.github.starwishsama.nbot.sessions.SessionManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.GroupMessageEvent

object SessionListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeMessages {
            always {
                if (!isPrefix(message.contentToString()) && SessionManager.isValidSession(sender.id)) {
                    val session : Session? = if (this is GroupMessageEvent && SessionManager.isValidSessionByGroup(group.id)) {
                        SessionManager.getSessionByGroup(group.id)
                    } else {
                        SessionManager.getSession(sender.id)
                    }

                    if (session != null) {
                        val command = session.command
                        if (command is WaitableCommand) {
                            var user = BotUser.getUser(sender.id)
                            if (user == null) {
                                user = BotUser.quickRegister(sender.id)
                            }
                            // 为了一些特殊需求, 请在命令中释放 Session
                            command.replyResult(this, user, session)
                        }
                    }
                }
            }
        }
    }

    override fun getName(): String = "会话"

    private fun isPrefix(message: String): Boolean {
        if (message.isNotEmpty()) {
            BotConstants.cfg.commandPrefix.forEach {
                if (message.startsWith(it)) {
                    return true
                }
            }
        }
        return false
    }
}