/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.datetime.Clock
import moe.sdl.yac.core.CommandResult
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.permission.PermissionManager
import ren.natsuyuk1.comet.api.permission.hasPermission
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.containsEtc
import ren.natsuyuk1.comet.utils.string.StringUtil.getLastingTimeAsString
import ren.natsuyuk1.comet.utils.string.StringUtil.replaceAll
import ren.natsuyuk1.comet.utils.string.StringUtil.toArgs
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

private val logger = mu.KotlinLogging.logger {}

object CommandManager {
    private val commands: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()

    private var commandScope = ModuleScope("CommandManager")
    fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        commandScope = ModuleScope("CommandManager", parentContext)
    }

    @Suppress("unused")
    fun registerCommand(
        entry: CommandProperty,
        handler: (Comet, PlatformCommandSender, MessageWrapper, CometUser) -> BaseCommand
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

        PermissionManager.register(commandNode.property.permission, commandNode.property.permissionLevel)
    }

    fun registerCommands(collection: Collection<AbstractCommandNode<*>>): Unit =
        collection.forEach { registerCommand(it) }

    /**
     * 调用命令
     *
     * @param sender 发送者
     * @param user [CometUser], 在调用后台命令时 **禁止** 调用!
     * @param rawMsg 原始消息
     */
    suspend fun executeCommand(
        comet: Comet,
        sender: CommandSender,
        wrapper: MessageWrapper
    ): Deferred<CommandStatus> = commandScope.async {
        val executeTime = Clock.System.now()

        if (wrapper.isEmpty()) {
            return@async CommandStatus.NotACommand()
        }

        val args = wrapper.parseToString().toArgs()

        if (sender !is ConsoleCommandSender && !args[0].containsEtc(false, comet.config.data.commandPrefix)) {
            return@async CommandStatus.NotACommand()
        }

        // TODO: 模糊搜索命令系统
        val cmd =
            getCommand(args[0].replaceAll(comet.config.data.commandPrefix)) ?: return@async CommandStatus.NotACommand()

        val property = cmd.property

        val result: CommandStatus = run {
            when (sender) {
                is ConsoleCommandSender -> {
                    if (cmd is ConsoleCommandNode) {
                        when (val cmdStatus =
                            cmd.handler(comet, sender, wrapper, CometUser.dummyUser).main(args.drop(1))) {
                            is CommandResult.Error -> {
                                logger.warn(cmdStatus.cause) { "在执行命令时发生了意外, ${cmdStatus.message}" }
                                return@run CommandStatus.Error()
                            }
                            is CommandResult.Success -> {
                                return@run CommandStatus.Success()
                            }
                        }
                    } else {
                        return@run CommandStatus.Success()
                    }
                }
                is PlatformCommandSender -> {
                    if (cmd is CommandNode) {
                        val user: CometUser = transaction findUser@{
                            val findByQQ = CometUser.findByQQ(sender.id)
                            if (findByQQ.empty()) {
                                val findByTelegram = CometUser.findByTelegramID(sender.id)

                                if (!findByTelegram.empty()) {
                                    return@findUser findByTelegram.first()
                                } else {
                                    val className = sender::class.java.name
                                    return@findUser if (className.contains("mirai")) {
                                        CometUser.create(sender.id)
                                    } else if (className.contains("telegram")) {
                                        CometUser.create(tgID = sender.id)
                                    } else {
                                        null
                                    }
                                }
                            } else {
                                return@findUser findByQQ.first()
                            }
                        } ?: return@run CommandStatus.Success()

                        if (!user.hasPermission(property.permission)) {
                            return@run CommandStatus.NoPermission()
                        }

                        when (property.executeConsumeType) {
                            CommandConsumeType.COOLDOWN -> {
                                if (user.triggerCommandTime.plus(property.executeConsumePoint.seconds) >= executeTime) {
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

                        when (val cmdStatus = cmd.handler(comet, sender, wrapper, user).main(args.drop(1))) {
                            is CommandResult.Error -> {
                                logger.warn(cmdStatus.cause) { "在执行命令时发生了意外, ${cmdStatus.message}" }
                                return@run CommandStatus.Error()
                            }
                            is CommandResult.Success -> {
                                return@run CommandStatus.Success()
                            }
                        }
                    } else {
                        return@run CommandStatus.Success()
                    }
                }
            }

            return@run CommandStatus.Error()
        }

        if (result.isPassed()) {
            logger.debug { "命令 ${cmd.property.name} 执行状态 ${result.name}, 耗时 ${executeTime.getLastingTimeAsString(msMode = true)}" }
        }

        return@async result
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
