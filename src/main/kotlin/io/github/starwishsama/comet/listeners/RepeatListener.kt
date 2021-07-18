/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import kotlin.time.ExperimentalTime

object RepeatListener : NListener {
    override val eventToListen = listOf(GroupMessageEvent::class)

    private val repeatCachePool = mutableMapOf<Long, RepeatInfo>()

    @ExperimentalTime
    override fun listen(event: Event) {
        if (event is GroupMessageEvent && !event.group.isBotMuted && canRepeat(event.group.id)) {
            val groupId = event.group.id
            val repeatInfo = repeatCachePool[groupId]

            if (repeatInfo == null) {
                repeatCachePool[groupId] = RepeatInfo(mutableListOf(event.message))
                return
            }

            if (repeatInfo.handleRepeat(event.message)) {
                runBlocking { event.subject.sendMessage(doRepeat(repeatInfo.messageCache.last())) }
            }
        }
    }

    private fun doRepeat(message: MessageChain): MessageChain {
        // 避免复读过多图片刷屏
        val count = message.parallelStream().filter { it is Image }.count()

        if (count <= 1L) {
            val msgChain = ArrayList<Message>()

            message.forEach {
                val msg: SingleMessage = if (it is PlainText) {
                    PlainText(it.content.replace("我".toRegex(), "你"))
                } else {
                    it
                }
                msgChain.add(msg)
            }

            return msgChain.toMessageChain()
        }

        return EmptyMessageChain
    }

    private fun canRepeat(groupId: Long): Boolean {
        return GroupConfigManager.getConfigOrNew(groupId).canRepeat
    }

    override fun getName(): String = "复读机"
}

data class RepeatInfo(
    val messageCache: MutableList<MessageChain> = mutableListOf(),
    var hasRepeated: Boolean = false
) {
    fun handleRepeat(message: MessageChain): Boolean {
        if (messageCache.isEmpty()) {
            messageCache.add(message)
            return false
        }

        val last = messageCache.last()
        val lastSender = last.source.fromId

        if (lastSender == message.source.fromId || !last.contentEquals(message, ignoreCase = false, strict = true)) {
            messageCache.clear()
            return false
        }

        if (lastSender != message.source.fromId && last.contentEquals(message, ignoreCase = false, strict = true)) {
            messageCache.add(message)
        }

        if (messageCache.size > 1 && !hasRepeated) {
            hasRepeated = true
            return true
        }

        return false
    }
}