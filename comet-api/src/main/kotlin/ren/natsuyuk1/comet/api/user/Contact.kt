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

    abstract var namecard: String
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
    var name: String,

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

    abstract fun getMember(id: Long): GroupMember

    abstract suspend fun quit(): Boolean

    abstract operator fun contains(id: Long): Boolean

    operator fun contains(member: GroupMember): Boolean = member in members
}
