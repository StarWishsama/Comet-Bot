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
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel

/**
 * [CommandProperty]
 *
 * 一个命令的相关配置
 */
open class CommandProperty(
    /**
     * 命令名称
     */
    open val name: String,
    /**
     * 命令别名
     */
    open val alias: List<String> = listOf(),
    /**
     * 命令描述
     */
    val description: String,
    /**
     * 命令帮助文本, 不填则使用默认生成的文本
     */
    val helpText: String = "",
    /**
     * 命令权限节点
     */
    val permission: String = "comet.command.$name",
    /**
     * 命令权限等级
     */
    open val permissionLevel: UserLevel = UserLevel.USER,
    /**
     * 调用命令所需等待时长/消耗金币
     */
    val executeConsumePoint: Int = CometGlobalConfig.data.commandCoolDown,
    /**
     * 调用命令费用类型
     * @see [CommandConsumeType]
     */
    val executeConsumeType: CommandConsumeType = CommandConsumeType.COOLDOWN,
    /**
     * 执行命令时的额外权限检查
     */
    val extraPermissionChecker: suspend (CometUser, PlatformCommandSender) -> Boolean = { _, _ -> true },
)

data class SubCommandProperty(
    override val name: String,
    override val alias: List<String> = listOf(),
    val parentCommandProperty: CommandProperty,
    override val permissionLevel: UserLevel = parentCommandProperty.permissionLevel,
) : CommandProperty(name, alias, "", "", "${parentCommandProperty.permission}.$name", permissionLevel)
