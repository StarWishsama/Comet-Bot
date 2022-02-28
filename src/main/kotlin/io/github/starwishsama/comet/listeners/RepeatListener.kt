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

import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*

object RepeatListener : INListener {
    override val name: String
        get() = "复读机"

    private val repeatCachePool = mutableMapOf<Long, RepeatInfo>()

    @EventHandler
    fun listen(event: GroupMessageEvent) {
        if (GroupConfigManager.getConfig(event.group.id)?.canRepeat != true
            || event.group.isBotMuted
            || CommandManager.getCommandPrefix(event.message.contentToString()).isNotEmpty()
        ) {
            return
        }

        val groupId = event.group.id
        val repeatInfo = repeatCachePool[groupId]

        runBlocking {
            if (repeatInfo == null) {
                repeatCachePool[groupId] = RepeatInfo().also { it.handleRepeat(event.group, event.message) }
            } else {
                repeatInfo.handleRepeat(event.group, event.message)
            }
        }
    }
}

data class RepeatInfo(
    var counter: Int = 0,
    var pendingRepeatMessage: MessageChain = EmptyMessageChain
) {
    suspend fun handleRepeat(group: Group, message: MessageChain) {
        if (counter < 2) {
            // First time repeat here.
            if (pendingRepeatMessage == EmptyMessageChain) {
                pendingRepeatMessage = message
                counter++
                return
            }

            val previousSenderId = pendingRepeatMessage.source.fromId
            val currentSenderId = message.sourceOrNull?.fromId

            // Validate the current message is same as pending one
            if (previousSenderId != currentSenderId
                && pendingRepeatMessage.contentEquals(message, ignoreCase = false, strict = true)
            ) {
                counter++
            } else {
                // Repeat has been interrupted, reset the counter
                pendingRepeatMessage = EmptyMessageChain
                counter = 0
                return
            }
        } else {
            if (pendingRepeatMessage.contentEquals(message, ignoreCase = false, strict = true)) {
                group.sendMessage(pendingRepeatMessage)

                counter = 0
                pendingRepeatMessage = EmptyMessageChain
            } else {
                // Repeat has been interrupted, reset the counter
                pendingRepeatMessage = EmptyMessageChain
                counter = 0
                return
            }
        }
    }

    private fun processRepeatMessage(message: MessageChain): MessageChain {
        // 避免复读过多图片刷屏
        val count = message.filterIsInstance<Image>().count()

        if (count <= 2L) {
            val revampChain = ArrayList<Message>()

            message.forEach {
                val msg: SingleMessage = if (it is PlainText) {
                    PlainText(
                        it.content.replace("你".toRegex(), "他")
                            .replace("我".toRegex(), "你")
                    )
                } else {
                    it
                }
                revampChain.add(msg)
            }

            return revampChain.toMessageChain()
        } else {
            return EmptyMessageChain
        }
    }
}