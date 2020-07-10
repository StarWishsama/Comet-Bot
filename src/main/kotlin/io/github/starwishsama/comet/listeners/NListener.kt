package io.github.starwishsama.comet.listeners

import net.mamoe.mirai.Bot

interface NListener {
    fun register(bot: Bot)
    fun getName(): String
}