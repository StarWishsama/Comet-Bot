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
import io.github.starwishsama.comet.enums.UserLevel

data class CommandProps(
    val name: String,
    val aliases: List<String> = mutableListOf(),
    val description: String,
    val permission: String,
    val level: UserLevel,
    val consumerType: CommandExecuteConsumerType = CommandExecuteConsumerType.COOLDOWN,
    val consumePoint: Double = CometVariables.cfg.coolDownTime.toDouble(),
    val needRecall: Boolean = false,
)