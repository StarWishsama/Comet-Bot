package io.github.starwishsama.nbot.listeners

import cn.hutool.core.util.RandomUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import kotlin.math.pow

object RepeatListener : NListener {
    private var value: Long = 0
    override fun register(bot: Bot) {
        if (value == 0L) {
            value = System.nanoTime()
        }

        bot.subscribeGroupMessages {
            always {
                val min = 10.0.pow(value.toDouble()).toInt()
                val chance = RandomUtil.randomInt(min.div(2), min)
                handleRepeat(this, chance)
            }
        }
    }

    private suspend fun handleRepeat(event: GroupMessageEvent, chance: Int) {
        if (event.message[QuoteReply] == null && chance >= value) {
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