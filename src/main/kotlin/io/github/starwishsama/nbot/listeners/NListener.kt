package io.github.starwishsama.nbot.listeners

import net.mamoe.mirai.Bot

interface NListener {
    fun register(bot: Bot)
    fun getName(): String
}