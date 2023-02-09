/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.command

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import moe.sdl.yac.core.CliktError
import moe.sdl.yac.core.CommandResult
import moe.sdl.yac.core.parseToArgs
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.permission.PermissionManager
import ren.natsuyuk1.comet.api.permission.hasPermission
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.datetime.getLastingTimeAsString
import ren.natsuyuk1.comet.utils.string.StringUtil.containsEtc
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.comet.utils.string.StringUtil.replaceAllToBlank
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val logger = mu.KotlinLogging.logger {}

fun CometUser.hasCoolDown(triggerTime: Instant, coolDown: Duration = CometGlobalConfig.data.commandCoolDown.seconds) =
    userLevel != UserLevel.OWNER && triggerCommandTime.plus(coolDown) >= triggerTime

object CommandManager {
    private val commands: MutableMap<String, AbstractCommandNode<*>> = ConcurrentHashMap()
    private val runningCommands: MutableMap<UUID, MutableSet<CommandProperty>> = ConcurrentHashMap()

    private var commandScope = ModuleScope("CommandManager")

    fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        logger.info { "已加载命令管理器" }
        commandScope = ModuleScope("CommandManager", parentContext)
    }

    @Suppress("unused")
    fun registerCommand(
        entry: CommandProperty,
        subCommandProperty: List<SubCommandProperty> = listOf(),
        handler: (Comet, PlatformCommandSender, PlatformCommandSender, MessageWrapper, CometUser) -> BaseCommand
    ) {
        registerCommand(CommandNode(entry, subCommandProperty, handler))
    }

    /**
     * 通过命令节点 [CommandNode] 注册一个命令
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun registerCommand(commandNode: AbstractCommandNode<*>) {
        val name = commandNode.property.name

        commands.putIfAbsent(name, commandNode)?.also {
            logger.warn { "Command name '$name' conflict, please check command property." }
            return
        }

        PermissionManager.register(commandNode.property.permission, commandNode.property.permissionLevel)

        if (commandNode.subCommandProperty.isNotEmpty()) {
            commandNode.subCommandProperty.forEach {
                PermissionManager.register(it.permission, it.permissionLevel)
            }
        }
    }

    fun registerCommands(collection: Collection<AbstractCommandNode<*>>): Unit =
        collection.forEach { registerCommand(it) }

    /**
     * 调用命令
     *
     * @param comet 触发命令的 [Comet] 实例
     * @param sender 发送者
     * @param subject 发送来源, 非群聊时与 sender 一致
     * @param wrapper 消息内容 [MessageWrapper]
     */
    suspend fun executeCommand(
        comet: Comet,
        sender: CommandSender,
        subject: CommandSender,
        wrapper: MessageWrapper,
        tempPermission: Pair<UserLevel, String>? = null
    ): Job = commandScope.launch {
        val executeTime = Clock.System.now()

        if (wrapper.isEmpty()) {
            return@launch
        }

        val args = wrapper.encodeToString().parseToArgs()

        if (args.isEmpty()) {
            return@launch
        }

        // 检查消息是否含命令前缀
        if (sender !is ConsoleCommandSender && !args[0].containsEtc(
                false, *CometGlobalConfig.data.commandPrefix.toTypedArray()
            )
        ) {
            return@launch
        }

        val cmdName = args[0].replaceAllToBlank(CometGlobalConfig.data.commandPrefix)

        val command = getCommand(cmdName, sender) ?: return@launch

        // Debug command always avaliable
        if (comet.maintainenceMode && command.property.name != "debug") {
            return@launch
        }

        logger.info {
            "${sender.asString()} 正在执行命令 ${command.property.name}"
        }

        val property = command.property
        var user: CometUser = CometUser.dummyUser

        runCatching {
            if (sender is PlatformCommandSender) {
                user = CometUser.getUserOrCreate(sender.id, sender.platform)

                val userLevel = tempPermission?.first ?: user.userLevel
                val hasPermission =
                    user.hasPermission(property.permission) ||
                        tempPermission?.second?.let {
                            it >= property.permission
                        } ?: false

                if (userLevel != UserLevel.OWNER) {
                    when (property.executeConsumeType) {
                        CommandConsumeType.COOLDOWN -> {
                            if (user.platform.needRestrict) {
                                if (user.hasCoolDown(executeTime, property.executeConsumePoint.seconds)) {
                                    return@runCatching CommandStatus.ValidateFailed()
                                }
                            }
                        }

                        CommandConsumeType.COIN -> {
                            if (user.coin < property.executeConsumePoint) {
                                return@runCatching CommandStatus.ValidateFailed()
                            } else {
                                user.coin = user.coin - property.executeConsumePoint
                            }
                        }
                    }
                }

                if (runningCommands[user.id.value]?.contains(property) == true) {
                    subject.sendMessage(buildMessageWrapper { appendText("上一条相同命令还在执行中哦") })
                    return@runCatching CommandStatus.Running()
                }

                runningCommands.getOrPut(user.id.value) { mutableSetOf(property) }.add(property)

                if (!hasPermission || !property.extraPermissionChecker(user, sender)) {
                    subject.sendMessage(buildMessageWrapper { appendText("你没有权限执行这条命令!") })
                    return@runCatching CommandStatus.NoPermission()
                }

                commandScope.launch {
                    transaction {
                        user.triggerCommandTime = executeTime
                    }
                }
            }

            val cmdStatus = if (sender is PlatformCommandSender) {
                (command as CommandNode).handler(comet, sender, subject as PlatformCommandSender, wrapper, user)
                    .main(args.drop(1))
            } else {
                (command as ConsoleCommandNode).handler(
                    comet, sender as ConsoleCommandSender, subject as ConsoleCommandSender, wrapper, user
                ).main(args.drop(1))
            }

            return@runCatching when (cmdStatus) {
                is CommandResult.Success -> {
                    CommandStatus.Success()
                }

                is CommandResult.Error -> {
                    subject.sendMessage(buildMessageWrapper { cmdStatus.userMessage?.let { appendText(it) } })

                    if (cmdStatus.cause !is CliktError) {
                        logger.warn(cmdStatus.cause) { "在执行命令时发生了意外" }
                    }

                    CommandStatus.Error()
                }
            }
        }.onSuccess {
            if (it.isPassed()) {
                logger.info {
                    "命令 ${command.property.name} 执行状态 ${it.name}, 耗时 ${
                    executeTime.getLastingTimeAsString(
                        TimeUnit.MILLISECONDS
                    )
                    }"
                }
            }
        }.onFailure {
            if (user.userLevel == UserLevel.OWNER) {
                subject.sendMessage(
                    buildMessageWrapper {
                        appendText(
                            "在尝试执行命令时发生异常, 报错信息如下, 详细请查看后台\n" + it::class.jvmName + ":" + (
                                it.message?.limit( // ktlint-disable max-line-length
                                    30
                                ) ?: "无"
                                )
                        )
                    }
                )
            } else {
                subject.sendMessage(buildMessageWrapper { appendText("这条命令突然坏掉了 (っ °Д °;)っ") })
            }

            logger.warn(it) { "在尝试执行命令 ${command.property.name} 时出现异常" }
        }

        runningCommands[user.id.value]?.apply {
            removeIf { it == property }

            if (isEmpty()) {
                runningCommands.remove(user.id.value)
            }
        }
    }

    /**
     * 通过主命令名或命令别称获取当前命令
     *
     * @param name 主命令名或命令别称
     * @return 命令节点, 不存在时为空
     */
    fun getCommand(name: String, sender: CommandSender): AbstractCommandNode<*>? {
        return when (sender) {
            is ConsoleCommandSender -> {
                if (commands[name] is ConsoleCommandNode) commands[name]
                else commands.values.find {
                    it is ConsoleCommandNode && it.property.alias.contains(name)
                }
            }

            is PlatformCommandSender -> {
                if (commands[name] is CommandNode) commands[name]
                else commands.values.find {
                    it is CommandNode && it.property.alias.contains(name)
                }
            }
        }
    }

    /**
     * 是否存在对应名称的命令
     *
     * @param name 主命令名或命令别称
     * @return 命令是否存在
     */
    fun hasCommand(name: String): Boolean =
        commands[name] != null || commands.values.any { it.property.alias.contains(name) }

    fun getCommands(): Map<String, AbstractCommandNode<*>> = commands
}
