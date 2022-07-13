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
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.NormalMember
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.util.toMessageChain
import ren.natsuyuk1.comet.utils.message.MessageWrapper

fun Member.toGroupMember(comet: MiraiComet): GroupMember {
    return when (this) {
        is NormalMember -> this.toGroupMember(comet)
        is AnonymousMember -> this.toGroupMember(comet)
        else -> error("Unsupported mirai side member (${this::class.simpleName})")
    }
}

fun NormalMember.toGroupMember(comet: MiraiComet): GroupMember {
    val contact = this@toGroupMember

    return object : GroupMember() {
        override val platformName: String
            get() = "mirai"

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
            comet.scope.launch {
                contact.sendMessage(message.toMessageChain(contact))
            }
        }

        override val remark: String
            get() = this@toGroupMember.remark
        override val comet: Comet
            get() = comet

        override val name: String
            get() = contact.nick

        override var card: String
            get() = contact.nameCard
            set(value) {
                contact.nameCard = value
            }
    }
}

fun AnonymousMember.toGroupMember(comet: MiraiComet): GroupMember {
    val contact = this@toGroupMember

    return object : ren.natsuyuk1.comet.api.user.AnonymousMember() {
        override val platformName: String
            get() = "mirai"

        override val anonymousId: String = contact.anonymousId

        override val id: Long
            get() = contact.id

        override var nameCard: String
            get() = contact.nameCard
            set(_) {
                error("Unsupported operation: Anomymous member cannot modify namecard")
            }

        /**
         * 匿名成员无此变量, 默认返回 -1
         */
        override val joinTimestamp: Int
            get() = -1

        /**
         * 匿名成员无此变量, 默认返回 -1
         */
        override val lastActiveTimestamp: Int
            get() = -1

        /**
         * 匿名成员无此变量, 默认返回 -1
         */
        override val remainMuteTime: Int
            get() = -1

        override suspend fun mute(seconds: Int) {
            contact.mute(seconds)
        }

        override suspend fun unmute() = contact.mute(0)

        override suspend fun kick(reason: String, block: Boolean) {
            error("AnonymousMember cannot be kicked")
        }

        override suspend fun operateAdminPermission(operation: Boolean) {
            error("AnonymousMember cannot be promoted")
        }

        override fun sendMessage(message: MessageWrapper) {
            error("Cannot send message to AnonymousMember")
        }

        override val remark: String
            get() = this@toGroupMember.remark
        override val comet: Comet
            get() = comet

        override val name: String
            get() = contact.nick

        override var card: String
            get() = contact.nameCard
            set(_) {
                error("Cannot modify namecard of AnonymousMember")
            }
    }
}

// FIXME: Unsafe convert (?)
fun ContactList<NormalMember>.toGroupMemberList(comet: MiraiComet): List<GroupMember> {
    val converted = mutableListOf<GroupMember>()
    for (normalMember in this) {
        converted.add(normalMember.toGroupMember(comet))
    }

    return converted
}
