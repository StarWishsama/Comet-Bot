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
import io.github.starwishsama.comet.objects.BotUser
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote

object AutoReplyListener : NListener {
    override val eventToListen = listOf(GroupMessageEvent::class)

    override fun listen(event: Event) {
        if (event is GroupMessageEvent) {
            event.apply {
                val cfg = GroupConfigManager.getConfig(group.id)

                if (cfg?.keyWordReply == null || cfg.keyWordReply.isEmpty()) return

                val user = BotUser.getUserOrRegister(sender.id)

                val currentTime = System.currentTimeMillis()

                val hasCoolDown = when (user.lastExecuteTime) {
                    -1L -> {
                        user.lastExecuteTime = currentTime
                        true
                    }
                    else -> {
                        val result = currentTime - user.lastExecuteTime < 5000
                        user.lastExecuteTime = currentTime
                        result
                    }
                }

                if (!hasCoolDown) return

                val messageContent = message.contentToString()

                cfg.keyWordReply.forEach {

                    if (it.keyWords.isEmpty()) return

                    it.keyWords.forEach { keyWord ->
                        if (messageContent.contains(keyWord)) {
                            runBlocking { subject.sendMessage(message.quote() + it.reply.toMessageChain(subject)) }
                            return
                        }
                    }
                }
            }
        }
    }

    override fun getName(): String = "关键词回复"
}