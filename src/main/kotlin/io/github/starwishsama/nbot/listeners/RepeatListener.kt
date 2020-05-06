package io.github.starwishsama.nbot.listeners

import cn.hutool.core.util.RandomUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.asMessageChain
import java.math.RoundingMode

object RepeatListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                if (message.get(QuoteReply) == null) {
                    val chance = RandomUtil.randomDouble(0.0, 1.0, 3, RoundingMode.HALF_DOWN)
                    val length = message.size
                    if (chance >= 0.7652 && length > RandomUtil.randomInt(1, 50)) {
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