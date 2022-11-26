/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.User

/**
 * [CommandSender]
 *
 *
 */
interface CommandSender {
    suspend fun sendMessage(message: MessageWrapper): MessageReceipt?
}

/**
 * [PlatformCommandSender]
 *
 * 不同平台实现的 `sendMessage`
 * 代表来自不同平台的用户
 */
abstract class PlatformCommandSender : CommandSender {

    /**
     * 获取到此用户的 [Comet]
     */
    abstract val comet: Comet

    /**
     * ID
     */
    abstract val id: Long

    /**
     * 用户名
     */
    abstract val name: String

    /**
     * 平台名称
     */
    abstract val platform: LoginPlatform

    abstract override suspend fun sendMessage(message: MessageWrapper): MessageReceipt?
}

/**
 * [ConsoleCommandSender]
 *
 * 代表来自终端的命令发送者
 */
object ConsoleCommandSender : CommandSender {
    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        println(message.parseToString())
        return null
    }
}

// region === cast ===

/**
 * Try casting [PlatformCommandSender] to [Contact] or return `null`
 *
 * @see [Contact]
 */
fun PlatformCommandSender.asContact() = this as? Contact

/**
 * Try casting [PlatformCommandSender] to [User] or return `null`
 *
 * @see [User]
 */
fun PlatformCommandSender.asUser() = this as? User

/**
 * Try casting [PlatformCommandSender] to [Group] or return `null`
 *
 * @see [Group]
 */
fun PlatformCommandSender.asGroup() = this as? Group

/**
 * Try casting [PlatformCommandSender] to [GroupMember] or return `null`
 *
 * @see [GroupMember]
 */
fun PlatformCommandSender.asMember() = this as? GroupMember

// endregion

// region === check ===

/**
 * Check [PlatformCommandSender] is [Contact] or not
 *
 * @see [Contact]
 */
fun PlatformCommandSender.isContact() = this is Contact

/**
 * Check [PlatformCommandSender] is [User] or not
 *
 * @see [User]
 */
fun PlatformCommandSender.isUser() = this is User

/**
 * Check [PlatformCommandSender] is [Group] or not
 *
 * @see [Group]
 */
fun PlatformCommandSender.isGroup() = this is GroupMember

/**
 * Check [PlatformCommandSender] is [GroupMember] or not
 *
 * @see [GroupMember]
 */
fun PlatformCommandSender.isMember() = this is GroupMember

// endregion

// region === equal ===
fun PlatformCommandSender.simpleEquals(other: PlatformCommandSender?): Boolean {
    if (other == null) {
        return false
    }

    return id == other.id && platform == other.platform
}
