package io.github.starwishsama.comet.test.listeners

import io.github.starwishsama.comet.listeners.RepeatInfo
import net.mamoe.mirai.message.data.MessageSourceBuilder
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.buildMessageChain
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class TestRepeatListener {
    @Test
    fun testRepeat() {
        val repeatInfo = RepeatInfo()

        repeat(2) {
            val message = buildMessageChain {
                add("好耶")
                add(MessageSourceBuilder().apply {
                    fromId = it.toLong()
                }.build(1, MessageSourceKind.GROUP))
            }

            repeatInfo.handleRepeat(message)
        }

        val last = buildMessageChain {
            add("好耶")
            add(MessageSourceBuilder().apply {
                fromId = 3
            }.build(1, MessageSourceKind.GROUP))
        }

        assertTrue("Repeat failed! repeatInfo: $repeatInfo") { repeatInfo.handleRepeat(last).isNotEmpty() }
    }
}