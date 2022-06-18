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
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.NormalMember
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.mirai.util.toMessageChain
import ren.natsuyuk1.comet.utils.message.MessageWrapper

fun net.mamoe.mirai.contact.NormalMember.toGroupMember(): GroupMember {
    val contact = this@toGroupMember

    return object : GroupMember() {
        override val id: Long
            get() = contact.id
        override var nameCard: String
            get() = contact.nameCard
            set(value) {
                contact.nameCard = value
            }
        override val joinTimestamp: Int
            get() = contact.joinTimestamp
        override val lastActiveTimestamp: Int
            get() = contact.lastSpeakTimestamp
        override val remainMuteTime: Int
            get() = contact.muteTimeRemaining

        override suspend fun mute(seconds: Int) {
            contact.mute(seconds)
        }

        override suspend fun unmute() = contact.unmute()

        override suspend fun kick(reason: String, block: Boolean) {
            contact.kick(reason, block)
        }

        override suspend fun operateAdminPermission(operation: Boolean) {
            contact.modifyAdmin(operation)
        }

        override fun sendMessage(message: MessageWrapper) {
            contactScope.launch {
                contact.sendMessage(message.toMessageChain(contact))
            }
        }

        override val name: String
            get() = contact.nick
        override val card: String
            get() = contact.nameCard

    }
}

// FIXME: Unsafe convert (?)
fun ContactList<NormalMember>.toGroupMemberList(): List<GroupMember> {
    val converted = mutableListOf<GroupMember>()
    for (normalMember in this) {
        converted.add(normalMember.toGroupMember())
    }

    return converted
}
