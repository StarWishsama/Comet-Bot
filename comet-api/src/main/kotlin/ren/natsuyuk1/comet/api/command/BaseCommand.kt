/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import moe.sdl.yac.core.CliktCommand
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.message.MessageWrapper

/**
 * [BaseCommand]
 *
 * Comet 的命令
 *
 * @param raw 获取转换为字符串后原始消息
 * @param message 获取由 [MessageWrapper] 包装后的信息, 包含非纯文本信息
 * @param user 调用该命令的用户
 */
abstract class BaseCommand(
    open val sender: CommandSender,
    message: MessageWrapper,
    user: CometUser,
    /**
     * 该命令的配置 [CommandProperty]
     */
    property: CommandProperty,
    option: CliktOption = CliktOption()
) : CliktCommand(
    name = property.name,
    help = property.helpText,
    invokeWithoutSubcommand = option.invokeWithoutSubCommand,
    printHelpOnEmptyArgs = option.printHelpOnEmptyArgs,
    allowMultipleSubcommands = option.allowMultipleSubcommands,
    treatUnknownOptionsAsArgs = option.treatUnknownOptionsAsArgs,
) {
    class CliktOption(
        val invokeWithoutSubCommand: Boolean = false,
        val printHelpOnEmptyArgs: Boolean = false,
        val allowMultipleSubcommands: Boolean = false,
        val treatUnknownOptionsAsArgs: Boolean = false,
    )
}

abstract class CometCommand(
    val comet: Comet,
    override val sender: CommandSender,
    message: MessageWrapper,
    user: CometUser,
    /**
     * 该命令的配置 [CommandProperty]
     */
    property: CommandProperty,
) : BaseCommand(sender, message, user, property)
