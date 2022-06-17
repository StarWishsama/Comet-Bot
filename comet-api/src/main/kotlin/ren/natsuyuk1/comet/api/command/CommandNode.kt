/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import ren.natsuyuk1.comet.api.user.CometUser

/**
 * [AbstractCommandNode]
 *
 * 代表一个命令处理节点
 *
 * @param property 命令参数
 * @param handler 命令处理传入参数的处理器
 */
abstract class AbstractCommandNode<CSender : CommandSender>(
    val property: CommandProperty,
    val handler: (sender: CSender, user: CometUser) -> CometCommand,
)

/**
 * [CommandNode]
 *
 * 一个 [CometCommand] 命令的处理节点
 */
class CommandNode(
    property: CommandProperty,
    handler: (sender: CommandSender, user: CometUser) -> CometCommand
) : AbstractCommandNode<CommandSender>(property, handler)

/**
 * [ConsoleCommandNode]
 *
 * 一个 [ConsoleCommandSender] 命令的处理节点
 */
class ConsoleCommandNode(
    property: CommandProperty,
    handler: (sender: ConsoleCommandSender, user: CometUser) -> CometCommand
) : AbstractCommandNode<ConsoleCommandSender>(property, handler)