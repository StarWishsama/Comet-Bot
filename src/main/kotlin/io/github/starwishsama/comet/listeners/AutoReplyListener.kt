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
import io.github.starwishsama.comet.objects.CometUser
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote

object AutoReplyListener : INListener {
    override val name: String
        get() = "关键词回复"

    @EventHandler
    fun listen(event: GroupMessageEvent) {
        event.apply {
            val cfg = GroupConfigManager.getConfig(group.id)

            if (cfg?.keyWordReply == null || cfg.keyWordReply.isEmpty()) return

            val user = CometUser.getUserOrRegister(sender.id)

            if (!user.checkCoolDown()) {
                return
            }

            val messageContent = message.contentToString()

            cfg.keyWordReply.forEach {
                if (it.keyWord.isEmpty() || it.reply.isEmpty()) {
                    return
                }

                if (messageContent.matches(it.keyWord.toRegex())) {
                    runBlocking { subject.sendMessage(message.quote() + it.reply.toMessageChain(subject)) }
                }
            }
        }
    }
}