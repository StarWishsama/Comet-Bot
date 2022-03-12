package io.github.starwishsama.comet.listeners

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.utils.doFilter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MessagePreSendEvent
import net.mamoe.mirai.message.data.toMessageChain

object NormalizeMessageSendListener: INListener {
    override val name: String = "消息发送处理"

    @EventHandler
    fun listen(event: MessagePreSendEvent) {
        event.message = event.message.toMessageChain().doFilter()

        runBlocking { delay(event.message.contentToString().calculateDelay()) }
    }

    private fun String.calculateDelay(): Long {
        val length = this.length

        return if (length in 1..30) {
            RandomUtil.randomLong(800, 3000)
        } else {
            5000
        }
    }
}