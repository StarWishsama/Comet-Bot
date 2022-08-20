/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.user

import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * [Contact] 联系人, 是所有可聊天对象的父类
 */
abstract class Contact : PlatformCommandSender() {
    /**
     * 可以是用户或群聊
     */
    abstract override val id: Long
}

/**
 * [User] 用户
 *
 */
abstract class User : Contact() {
    abstract override val id: Long
}

abstract class GroupMember : User() {
    abstract val group: Group

    abstract override val id: Long

    abstract val joinTimestamp: Int

    abstract val lastActiveTimestamp: Int

    abstract val remainMuteTime: Int

    abstract val card: String

    abstract val groupPermission: GroupPermission

    val isMuted: Boolean get() = remainMuteTime != 0

    /**
     * 禁言此群员
     */
    abstract suspend fun mute(seconds: Int)

    suspend fun mute(duration: Duration) {
        require(duration.toDouble(DurationUnit.DAYS) <= 30) { "max duration is 1 month" }
        require(duration.toDouble(DurationUnit.SECONDS) > 0) { "min duration is 1 second" }

        mute(duration.toDouble(DurationUnit.SECONDS).toInt())
    }

    /**
     * 解禁此群员
     *
     * @throws PermissionDeniedException
     */
    abstract suspend fun unmute()

    /**
     * 踢出此群员
     *
     * @param reason 踢出原因
     * @param block 是否拉黑
     *
     * @throws PermissionDeniedException
     */
    abstract suspend fun kick(reason: String, block: Boolean)

    suspend fun kick(reason: String) = kick(reason, false)

    /**
     * 给予该群员管理员权限.
     *
     * @param operation 是否给予
     */
    abstract suspend fun operateAdminPermission(operation: Boolean)

    abstract override fun sendMessage(message: MessageWrapper)
}

fun GroupMember.nameOrCard(): String = card.ifEmpty { name }

fun GroupMember.isOperator() = groupPermission >= GroupPermission.ADMIN

fun GroupMember.isAdmin() = groupPermission == GroupPermission.ADMIN

fun GroupMember.isOwner() = groupPermission == GroupPermission.OWNER

abstract class AnonymousMember : GroupMember() {
    /**
     * 该匿名群成员 ID
     */
    abstract val anonymousId: String
}

/**
 * [Group] 群组
 */
abstract class Group(
    override val id: Long,

    /**
     * 群名称
     */
    override var name: String
) : Contact() {
    abstract val owner: GroupMember

    abstract val members: List<GroupMember>

    abstract fun updateGroupName(groupName: String)

    /**
     * 仅在 Mirai 环境下可用, Telegram 实现会返回 -1
     */
    abstract fun getBotMuteRemaining(): Int

    abstract fun getBotPermission(): GroupPermission

    abstract val avatarUrl: String

    abstract fun getMember(id: Long): GroupMember?

    abstract suspend fun quit(): Boolean

    abstract operator fun contains(id: Long): Boolean

    operator fun contains(member: GroupMember): Boolean = member in members
}
