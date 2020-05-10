package io.github.starwishsama.nbot.listeners

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.interfaces.WaitableCommand
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.SessionManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.GroupMessageEvent

object SessionListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeMessages {
            always {
                if (!isPrefix(message.contentToString())) {
                    if (SessionManager.isValidSession(sender.id)) {
                        val session = SessionManager.getSession(sender.id)
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
                    } else if (this is GroupMessageEvent && SessionManager.isValidSessionByGroup(group.id)) {
                        val session = SessionManager.getSessionByGroup(group.id)
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
    }

    override fun getName(): String = "会话"

    private fun isPrefix(message: String): Boolean {
        if (message.isNotEmpty()) {
            for (prefix in BotConstants.cfg.commandPrefix) {
                if (message.startsWith(prefix)) {
                    return true
                }
            }
        }
        return false
    }
}