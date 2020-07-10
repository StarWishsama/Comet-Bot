package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.BotConstants
import io.github.starwishsama.comet.commands.interfaces.SuspendCommand
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeMessages

object SessionListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeMessages {
            always {
                if (!isPrefix(message.contentToString()) && SessionManager.isValidSessionById(sender.id)) {
                    val session: Session? = SessionManager.getSessionByEvent(this)

                    if (session != null) {
                        val command = session.command
                        if (command is SuspendCommand) {
                            var user = BotUser.getUser(sender.id)
                            if (user == null) {
                                user = BotUser.quickRegister(sender.id)
                            }

                            /** 为了一些特殊需求, 请在命令中释放 Session
                             * 见 [SuspendCommand]
                             */
                            command.handleInput(this, user, session)
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