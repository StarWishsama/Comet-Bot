/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.command

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.managers.PermissionManager
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.objects.permission.CometPermission
import io.github.starwishsama.comet.utils.StringUtil

data class CommandProps(
    val name: String,
    val aliases: List<String> = mutableListOf(),
    val description: String,
    val level: UserLevel,
    val permissionNodeName: String = "comet.command.$name",
    val consumerType: CommandExecuteConsumerType = CommandExecuteConsumerType.COOLDOWN,
    val consumePoint: Double = CometVariables.cfg.coolDownTime.toDouble(),
) {
    init {
        if (!StringUtil.isAlphabeticAndDigit(name)) {
            throw IllegalArgumentException("Command name must be alphabetic")
        }

        PermissionManager.registerPermission(
            CometPermission(permissionNodeName, level)
        )
    }

    fun isDisabledCommand(id: Long): Boolean {
        return GroupConfigManager.getConfig(id)?.disabledCommands?.contains(name) ?: false
    }

    fun disableCommand(id: Long): ConfigureCommandStatus {
        val command = CommandManager.getCommand(name)
        if (command != null) {
            if (command is UnDisableableCommand) {
                return ConfigureCommandStatus.UnDisabled
            }

            val disabledCommands =
                GroupConfigManager.getConfig(id)?.disabledCommands ?: return ConfigureCommandStatus.Error

            return if (!disabledCommands.contains(name)) {
                disabledCommands.add(name)
                ConfigureCommandStatus.Disabled
            } else {
                disabledCommands.remove(name)
                ConfigureCommandStatus.Enabled
            }
        } else {
            return ConfigureCommandStatus.NotExist
        }
    }

    sealed class ConfigureCommandStatus(val msg: String) {
        object UnDisabled : ConfigureCommandStatus("该命令无法被禁用!")
        object Enabled : ConfigureCommandStatus("成功启用该命令")
        object Disabled : ConfigureCommandStatus("成功禁用该命令")
        object NotExist : ConfigureCommandStatus("该命令不存在!")
        object Error : ConfigureCommandStatus("禁用失败, 发生了意外错误.")
    }
}