package io.github.starwishsama.comet.listeners

import net.mamoe.mirai.Bot

interface NListener {
    /**
     * @TODO 完善监听器以适配开关
     */
    fun register(bot: Bot)
    fun getName(): String
}