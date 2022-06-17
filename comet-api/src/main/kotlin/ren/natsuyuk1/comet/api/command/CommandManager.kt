/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import moe.sdl.yac.core.CommandResult
import ren.natsuyuk1.comet.api.permission.hasPermission
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.string.StringUtil.toArgs
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

private val logger = mu.KotlinLogging.logger {}

object CommandManager {
    private val commands: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()

    private var commandScope = ModuleScope("CommandManager")

    internal fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        commandScope = ModuleScope("CommandManager", parentContext)
    }

    @Suppress("unused")
    fun registerCommand(
        entry: CommandProperty,
        handler: (CommandSender, CometUser) -> CometCommand
    ) {
        registerCommand(CommandNode(entry, handler))
    }

    /**
     * 通过命令节点 [CommandNode] 注册一个命令
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun registerCommand(commandNode: AbstractCommandNode<*>) {
        val name = commandNode.property.name

        commands.putIfAbsent(name, commandNode)?.also {
            logger.warn { "Command name '$name' conflict, please check command property." }
        }
    }

    fun registerCommands(collection: Collection<AbstractCommandNode<*>>): Unit =
        collection.forEach { registerCommand(it) }

    suspend fun executeCommand(
        sender: CommandSender,
        user: CometUser,
        rawMsg: String,
    ): Job = commandScope.launch {
        if (rawMsg.isEmpty()) {
            return@launch
        }

        val args = rawMsg.toArgs()

        // TODO: 模糊搜索命令系统
        val cmd = getCommand(args[0]) ?: return@launch

        val property = cmd.property

        val result: CommandStatus = run {
            if (!user.hasPermission(property.permission)) {
                return@run CommandStatus.NoPermission()
            }

            val currentTime = Clock.System.now()

            when (property.executeConsumeType) {
                CommandConsumeType.COOLDOWN -> {
                    if (user.triggerCommandTime.plus(property.executeConsumePoint.seconds) >= currentTime) {
                        return@run CommandStatus.ValidateFailed()
                    }
                }

                CommandConsumeType.COIN -> {
                    if (user.coin < property.executeConsumePoint) {
                        return@run CommandStatus.ValidateFailed()
                    } else {
                        user.coin = user.coin - property.executeConsumePoint
                    }
                }
            }

            when (cmd) {
                is CommandNode -> {
                    when (val cmdStatus = cmd.handler(sender, user).main(args.drop(1))) {
                        is CommandResult.Error -> {
                            logger.warn(cmdStatus.cause) { "在执行命令时发生了意外, ${cmdStatus.message}" }
                            return@run CommandStatus.Error()
                        }
                        is CommandResult.Success -> {
                            return@run CommandStatus.Success()
                        }
                    }
                }

                is ConsoleCommandNode -> {
                    TODO()
                }
            }

            return@run CommandStatus.Failed()
        }
    }

    /**
     * 通过主命令名或命令别称获取当前命令
     *
     * @param name 主命令名或命令别称
     * @return 命令节点, 不存在时为空
     */
    fun getCommand(name: String): AbstractCommandNode<*>? =
        commands.filter { it.value.property.name == name || it.value.property.alias.contains(name) }.values.firstOrNull()

    /**
     * 是否存在对应名称的命令
     *
     * @param name 主命令名或命令别称
     * @return 命令是否存在
     */
    fun hasCommand(name: String): Boolean =
        commands.filter { it.value.property.name == name || it.value.property.alias.contains(name) }.isNotEmpty()

    fun getCommands(): Map<String, AbstractCommandNode<*>> = commands
}
