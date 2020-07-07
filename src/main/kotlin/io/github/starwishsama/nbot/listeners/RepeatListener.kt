package io.github.starwishsama.nbot.listeners

import cn.hutool.core.util.RandomUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import java.math.RoundingMode

object RepeatListener : NListener {
    private const val VALUE = 85.0

    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                val chance = RandomUtil.randomDouble(2, RoundingMode.HALF_DOWN)
                handleRepeat(this, chance)
            }
        }
    }

    private suspend fun handleRepeat(event: GroupMessageEvent, chance: Double) {
        if (event.message[QuoteReply] == null && chance >= VALUE) {
            // 避免复读过多图片刷屏
            val count = event.message.stream().filter { it is Image }.count()

            if (count <= 9) {
                val msgChain = ArrayList<Message>()

                event.message.forEach { msgChain.add(it) }

                for (i in 0 until msgChain.size) {
                    val chain = msgChain[i]
                    if (chain is PlainText) {
                        msgChain[i] = PlainText(chain.content.replace("我", "你"))
                    }
                }

                event.reply(msgChain.asMessageChain())
            }
        }
    }

    override fun getName(): String = "复读机"
}