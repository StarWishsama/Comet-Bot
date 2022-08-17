/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.event.impl.message.MessageEvent
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel

/**
 * [CommandProperty]
 *
 * 一个命令的相关配置
 */
open class CommandProperty(
    open val name: String,
    open val alias: List<String> = listOf(),
    val description: String,
    val helpText: String,
    val permission: String = "comet.command.${name}",
    open val permissionLevel: UserLevel = UserLevel.USER,
    val executeConsumePoint: Int = CometGlobalConfig.data.commandCoolDown,
    val executeConsumeType: CommandConsumeType = CommandConsumeType.COOLDOWN,
    val extraPermissionChecker: (CometUser, MessageEvent) -> Boolean = { _, _ -> true }
)

data class SubCommandProperty(
    override val name: String,
    override val alias: List<String>,
    val parentCommandProperty: CommandProperty,
    override val permissionLevel: UserLevel = UserLevel.USER
) : CommandProperty(name, alias, "", "", "${parentCommandProperty.permission}.$name", permissionLevel)
