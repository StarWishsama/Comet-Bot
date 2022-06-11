/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import java.util.concurrent.ConcurrentHashMap

private val logger = mu.KotlinLogging.logger {}

abstract class AbstractCommandNode<TSender : CommandSender>(
    val property: CommandProperty,
    val call: (sender: TSender) -> CometCommand,
)

class CommandNode(
    property: CommandProperty,
    call: (CommandSender) -> CometCommand
) : AbstractCommandNode<CommandSender>(property, call)

class ConsoleCommandNode(
    property: CommandProperty,
    call: (ConsoleCommandSender) -> CometCommand
) : AbstractCommandNode<ConsoleCommandSender>(property, call)

object CommandManager {
    private val commands: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()

    private var commandScope = ModuleScope("CommandManager")

    internal fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        commandScope = ModuleScope("CommandManager", parentContext)
    }
}
