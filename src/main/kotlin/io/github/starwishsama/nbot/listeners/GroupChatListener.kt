package io.github.starwishsama.nbot.listeners

import io.github.starwishsama.nbot.objects.BotUser
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

object GroupChatListener {
    fun register(bot: Bot) {
        bot.logger.info("[监听器] 已注册 群聊聊天 监听器")
        bot.subscribeGroupMessages {
            always {
                BotUser.getUser(sender.id)?.let { user ->
                    user.flower.let { flower ->
                        run {
                            val length = message.contentToString().length
                            when {
                                length >= 40 -> {
                                    flower?.energy.let {
                                        it?.plus(length.toDouble() / 20 * 1.5)
                                    }
                                }
                                length in 20 until 40 -> {
                                    flower?.energy.let {
                                        it?.plus(length.toDouble() / 20 * 1.3)
                                    }
                                }
                                else -> {
                                    flower?.energy.let {
                                        it?.plus(length.toDouble() / 20)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}