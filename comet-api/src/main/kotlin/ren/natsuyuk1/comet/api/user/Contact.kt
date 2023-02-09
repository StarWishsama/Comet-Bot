/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.user

import kotlinx.datetime.Instant
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.AtElement
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * [Contact] 联系人, 是所有可聊天对象的父类
 */
interface Contact : PlatformCommandSender

/**
 * [User] 用户
 *
 */
interface User : Contact

/**
 * 好友, 仅在 QQ 平台使用，用于识别
 */
interface Friend : User

/**
 * 陌生人, 仅在 QQ 平台使用，用于识别
 */
interface Stranger : User

interface GroupMember : User {
    val group: Group
    val card: String

    suspend fun getGroupPermission(): GroupPermission

    suspend fun mute(seconds: Int)

    suspend fun mute(duration: Duration) = mute(duration.toDouble(DurationUnit.SECONDS).toInt())

    suspend fun getJoinTime(): Instant

    suspend fun getLastActiveTime(): Instant

    suspend fun getRemainMuteTime(): Duration

    suspend fun unmute()

    suspend fun kick(reason: String, block: Boolean)

    suspend fun kick(reason: String) = kick(reason, false)

    suspend fun operateAdminPermission(operation: Boolean)
}

suspend fun GroupMember.isFriend() = comet.getFriend(id) != null

suspend fun GroupMember.isStranger() = comet.getStranger(id) != null

fun GroupMember.at(): AtElement =
    if (platform == CometPlatform.TELEGRAM) {
        AtElement(userName = name)
    } else {
        AtElement(id)
    }

fun GroupMember.nameOrCard(): String = card.ifEmpty { name }

suspend fun GroupMember.isOperator() = getGroupPermission() >= GroupPermission.ADMIN

suspend fun GroupMember.isAdmin() = getGroupPermission() == GroupPermission.ADMIN

suspend fun GroupMember.isOwner() = getGroupPermission() == GroupPermission.OWNER

suspend fun GroupMember.asFriend() = comet.getFriend(id)

suspend fun GroupMember.asStranger() = comet.getStranger(id)

interface AnonymousMember : GroupMember {
    /**
     * 该匿名群成员 ID
     */
    val anonymousId: String
}

/**
 * [Group] 群组
 */
interface Group : Contact {
    suspend fun getOwner(): GroupMember

    suspend fun getMembers(): List<GroupMember>

    suspend fun updateGroupName(groupName: String)

    suspend fun getBotMuteRemaining(): Int

    suspend fun getBotPermission(): GroupPermission

    suspend fun getGroupAvatarURL(): String

    suspend fun getMember(id: Long): GroupMember?

    suspend fun quit(): Boolean

    suspend fun contains(id: Long): Boolean
}
