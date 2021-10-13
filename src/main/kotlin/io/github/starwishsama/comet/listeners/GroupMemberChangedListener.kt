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
import io.github.starwishsama.comet.objects.wrapper.AtElement
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.PureText
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MemberJoinEvent

@NListener("群聊欢迎")
object GroupMemberChangedListener : INListener {

    @EventHandler
    fun listen(event: MemberJoinEvent) {
        val cfg = GroupConfigManager.getConfig(event.groupId) ?: return

        if (cfg.newComerWelcome && !cfg.newComerWelcomeText.isEmpty()) {
            runBlocking {
                event.group.sendMessage(
                    reWrapMessage(
                        cfg.newComerWelcomeText,
                        event.user.id
                    ).toMessageChain(event.group)
                )
            }
        }
    }

    private fun reWrapMessage(original: MessageWrapper, memberID: Long): MessageWrapper {
        val newWrapper = MessageWrapper()

        for (wrapperElement in original.getMessageContent()) {
            if (wrapperElement is PureText) {
                val index = wrapperElement.text.indexOf("[At]")
                if (index > -1) {
                    val before = wrapperElement.text.substring(0, index)
                    val after = wrapperElement.text.substring(index)

                    newWrapper.addText("$before ")
                    newWrapper.addElement(AtElement(memberID))
                    newWrapper.addText(" $after")
                } else {
                    newWrapper.addElement(wrapperElement)
                }
            } else {
                newWrapper.addElement(wrapperElement)
            }
        }

        return newWrapper
    }
}