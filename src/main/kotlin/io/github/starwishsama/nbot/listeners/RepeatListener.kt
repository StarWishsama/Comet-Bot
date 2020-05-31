package io.github.starwishsama.nbot.listeners

import cn.hutool.core.util.RandomUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.asMessageChain

object RepeatListener : NListener {
    override fun register(bot: Bot) {
        val value = RandomUtil.randomInt(1_000, 10_000)
        bot.subscribeGroupMessages {
            always {
                if (message[QuoteReply] == null) {
                    val chance = RandomUtil.randomInt(0, 10_000)
                    val length = message.size
                    if (chance >= value && length > RandomUtil.randomInt(1, 50)) {
                        val msgChain = ArrayList<Message>()

                        message.forEach { msgChain.add(it) }
                        for (i in 0 until msgChain.size) {
                            val chain = msgChain[i]
                            if (chain is PlainText) {
                                msgChain[i] = PlainText(chain.content.replace("我", "你"))
                            }
                        }

                        reply(msgChain.asMessageChain())
                    }
                }
            }
        }
    }

    override fun getName(): String = "复读机"
}