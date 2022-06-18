/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.user

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * [Contact] 联系人, 是所有可聊天对象的父类
 */
@Serializable
abstract class Contact : PlatformCommandSender() {
    /**
     * 可以是用户或群聊
     */
    abstract val id: Long
}

/**
 * [User] 用户
 *
 */
@Serializable
abstract class User : Contact() {
    abstract override val id: Long

    /**
     * 备注信息
     *
     * 当该用户与机器人存在好友关系时才有备注，否则为空
     */
    abstract val remark: String
}

@Serializable
abstract class GroupMember : Contact() {
    abstract override val id: Long

    abstract var nameCard: String

    abstract val joinTimestamp: Int

    abstract val lastActiveTimestamp: Int

    abstract val remainMuteTime: Int

    val isMuted: Boolean get() = remainMuteTime != 0

    /**
     * 禁言此群员1
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

/**
 * [Group] 群组
 */
@Serializable
abstract class Group(
    override val id: Long,

    /**
     * 群名称
     */
    override var name: String,

    val owner: GroupMember,

    val members: List<GroupMember>
) : Contact() {
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
