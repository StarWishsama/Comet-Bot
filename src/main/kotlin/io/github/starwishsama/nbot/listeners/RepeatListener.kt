package io.github.starwishsama.nbot.listeners

import cn.hutool.core.util.RandomUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

object RepeatListener {
    fun register(bot : Bot){
        bot.logger.info("[监听器] 已注册 复读 监听器")
        bot.subscribeGroupMessages {
            always {
                val chance = RandomUtil.randomInt(0, 1000)
                val length = message.size
                if (chance in 532 until 655 && length > RandomUtil.randomInt(1, 50)){
                    reply(this.message)
                }
            }
        }
    }
}