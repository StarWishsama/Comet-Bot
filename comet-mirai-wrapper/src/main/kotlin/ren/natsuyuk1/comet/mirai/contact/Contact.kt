/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.mirai.contact

import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Group
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.mirai.util.toMessageChain
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.message.MessageWrapper

private val scope = ModuleScope("comet_contact_wrapper")

fun Group.toCometGroup(): ren.natsuyuk1.comet.api.user.Group {
    val group = this@toCometGroup

    return object : ren.natsuyuk1.comet.api.user.Group(
        group.id,
        group.name,
        group.owner.toGroupMember(),
        group.members.toGroupMemberList()
    ) {
        override fun updateGroupName(groupName: String) {
            group.name = groupName
        }

        override fun getBotMuteRemaining(): Int = group.botMuteRemaining

        override fun getBotPermission(): GroupPermission {
            return GroupPermission.valueOf(group.botPermission.name)
        }

        override val avatarUrl: String = group.avatarUrl

        override val card: String = ""

        override fun sendMessage(message: MessageWrapper) {
            scope.launch {
                group.sendMessage(message.toMessageChain(group))
            }
        }
    }
}