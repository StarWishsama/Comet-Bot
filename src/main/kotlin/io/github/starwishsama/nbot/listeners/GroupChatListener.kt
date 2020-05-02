package io.github.starwishsama.nbot.listeners

import io.github.starwishsama.nbot.objects.BotUser
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

object GroupChatListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                val user = BotUser.getUser(sender.id)

                if (user != null) {
                    val flower = user.flower
                    if (flower != null) {
                        val length = message.contentToString().length
                        when {
                            length >= 50 -> flower.addEnergy(length / 20 * 1.4)
                            length in 20 until 50 -> flower.addEnergy(length / 20 * 1.2)
                            else -> flower.addEnergy(length / 20 * 1.0)
                        }
                    }
                }
            }
        }
    }

    override fun getName(): String = "群聊聊天"
}