package io.github.starwishsama.nbot.listeners

import cn.hutool.core.util.RandomUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import java.math.RoundingMode

object RepeatListener : NListener {
    override fun register(bot: Bot) {
        bot.subscribeGroupMessages {
            always {
                val chance = RandomUtil.randomDouble(0.0, 1.0, 3, RoundingMode.HALF_DOWN)
                val length = message.size
                if (chance >= 0.8572 && length > RandomUtil.randomInt(1, 50)) {
                    reply(message)
                }
            }
        }
    }

    override fun getName(): String = "复读机"
}