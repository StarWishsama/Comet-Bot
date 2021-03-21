package io.github.starwishsama.comet.api.command

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.*
import java.time.LocalDateTime
import java.util.*

/**
 * 彗星 Bot 命令处理器
 *
 * 处理群聊/私聊聊天信息中存在的命令
 * @author Nameless
 */
object CommandExecutor {
    private val commands: MutableSet<ChatCommand> = mutableSetOf()
    private val consoleCommands = mutableListOf<ConsoleCommand>()

    /**
     * 注册命令
     *
     * @param command 要注册的命令
     */
    private fun setupCommand(command: ChatCommand) {
        if (command.canRegister() && !commands.add(command)) {
            BotVariables.logger.warning("[命令] 正在尝试注册已有命令 ${command.getProps().name}")
        }
    }

    /**
     * 注册命令
     *
     * @param commands 要注册的命令集合
     */
    @Suppress("unused")
    fun setupCommand(commands: Array<ChatCommand>) {
        commands.forEach {
            if (!CommandExecutor.commands.contains(it) && it.canRegister()) {
                CommandExecutor.commands.add(it)
            }
        }
    }

    fun setupCommand(commands: Array<Any>) {
        commands.forEach {
            when (it) {
                is ChatCommand -> setupCommand(it)
                is ConsoleCommand ->
                    if (!consoleCommands.contains(it)) {
                        consoleCommands.plusAssign(it)
                    } else {
                        BotVariables.logger.warning("[命令] 正在尝试注册已有后台命令 ${it.getProps().name}")
                    }
                else -> {
                    BotVariables.logger.warning("[命令] 正在尝试注册非命令类 ${it.javaClass.simpleName}")
                }
            }
        }
    }

    fun startHandler(bot: Bot) {
        bot.eventChannel.subscribeMessages {
            always {
                val executedTime = LocalDateTime.now()
                if (sender.id != 80000000L) {
                    if (this is GroupMessageEvent && group.isBotMuted) return@always

                    val result = dispatchCommand(this)

                    try {
                        if (result.status.isOk() && result.msg !is EmptyMessageChain) {
                            this.subject.sendMessage(result.msg)
                        }
                    } catch (e: IllegalArgumentException) {
                        BotVariables.logger.warning("正在尝试发送空消息, 执行的命令为 $result")
                    }

                    if (result.status.isOk()) {
                        BotVariables.logger.debug(
                                "[命令] 命令执行耗时 ${executedTime.getLastingTimeAsString(msMode = true)}" +
                                        if (result.status.isOk()) ", 执行结果: ${result.status.name}" else ""
                        )
                    }
                }
            }
        }
    }

    /**
     * 执行消息中的命令
     *
     * @param event Mirai 消息命令 (聊天)
     */
    private suspend fun dispatchCommand(event: MessageEvent): ExecutedResult {
        val senderId = event.sender.id
        val message = event.message.contentToString()

        val cmd = getCommand(getCommandName(message))

        val useDebug = cmd?.getProps()?.name?.contentEquals("debug") == true

        val user = BotUser.getUserOrRegister(senderId)

        if (BotVariables.switch || useDebug) {
            try {

                /**
                 * 如果不是监听会话, 则停止尝试解析并执行可能的命令
                 * 反之在监听时仍然可以执行命令
                 */
                if (SessionHandler.handleSessions(event, user)) {
                    return ExecutedResult(EmptyMessageChain, cmd, CommandStatus.MoveToSession())
                }

                /**
                 * 检查是否在尝试执行被禁用命令
                 */
                if (cmd != null && event is GroupMessageEvent &&
                        GroupConfigManager.getConfig(event.group.id)?.isDisabledCommand(cmd) == true) {
                    return if (validateStatus(user, cmd.getProps())) {
                        ExecutedResult(CometUtil.toChain("该命令已被管理员禁用"), cmd, CommandStatus.Disabled())
                    } else {
                        ExecutedResult(EmptyMessageChain, cmd, CommandStatus.Disabled())
                    }
                }

                val prefix = getCommandPrefix(message)

                if (prefix.isNotEmpty() && cmd != null) {
                    // 前缀末尾下标
                    val index = message.indexOf(prefix) + prefix.length

                    var splitMessage = message.substring(index, message.length).split(" ")
                    splitMessage = splitMessage.subList(1, splitMessage.size)

                    BotVariables.logger.debug("[命令] $senderId 尝试执行命令: ${cmd.name} (原始消息: ${message})")

                    val status: CommandStatus

                    /** 检查是否有权限执行命令 */
                    val result: MessageChain = if (cmd.hasPermission(user, event)) {
                        status = CommandStatus.Success()
                        cmd.execute(event, splitMessage, user)
                    } else {
                        status = CommandStatus.NoPermission()
                        CometUtil.toChain("你没有权限!")
                    }

                    return ExecutedResult(result, cmd, status)
                }
            } catch (t: Throwable) {
                return if (NetUtil.isTimeout(t)) {
                    BotVariables.logger.warning("执行网络操作失败: ", t)
                    ExecutedResult("Bot > 在执行网络操作时连接超时: ${t.message ?: ""}".convertToChain(), cmd)
                } else {
                    BotVariables.logger.warning("[命令] 在试图执行命令时发生了一个错误, 原文: ${message}, 发送者: $senderId", t)
                    if (user.isBotOwner()) {
                        ExecutedResult(CometUtil.toChain("在试图执行命令时发生了一个错误\n简易报错信息 :\n${t.javaClass.name}: ${t.message?.limitStringSize(30)}"), cmd, CommandStatus.Failed())
                    } else {
                        ExecutedResult(CometUtil.toChain("在试图执行命令时发生了一个错误, 请联系管理员"), cmd, CommandStatus.Failed())
                    }
                }
            }
        }
        return ExecutedResult(EmptyMessageChain, cmd)
    }

    /**
     * 执行后台命令
     *
     * @param content 纯文本命令 (后台)
     */
    suspend fun dispatchConsoleCommand(content: String): String {
        try {
            val cmd = getConsoleCommand(getCommandName(content))
            if (cmd != null) {
                val splitMessage = content.split(" ")
                val splitCommand = splitMessage.subList(1, splitMessage.size)
                BotVariables.logger.debug("[命令] 后台尝试执行命令: " + cmd.getProps().name)
                return cmd.execute(splitCommand)
            }
        } catch (t: Throwable) {
            BotVariables.logger.warning("[命令] 在试图执行命令时发生了一个错误, 原文: $content", t)
            return ""
        }
        return ""
    }

    fun getCommand(cmdPrefix: String): ChatCommand? {
        val command = commands.parallelStream().filter {
            isCommandNameEquals(it, cmdPrefix)
        }.findFirst()

        return if (command.isPresent) command.get() else null
    }

    private fun getConsoleCommand(cmdPrefix: String): ConsoleCommand? {
        for (command in consoleCommands) {
            if (isCommandNameEquals(command, cmdPrefix)) {
                return command
            }
        }
        return null
    }

    private fun getCommandName(message: String): String {
        val cmdPrefix = getCommandPrefix(message)

        val index = message.indexOf(cmdPrefix) + cmdPrefix.length

        return message.substring(index, message.length).split(" ")[0]
    }

    /**
     * 消息开头是否为命令前缀
     *
     * @return 消息前缀, 不匹配返回空
     */
    fun getCommandPrefix(message: String): String {
        if (message.isNotEmpty()) {
            BotVariables.cfg.commandPrefix.forEach {
                if (message.startsWith(it)) {
                    return it
                }
            }
        }

        return ""
    }

    private fun isCommandNameEquals(cmd: ChatCommand, cmdName: String): Boolean {
        val props = cmd.getProps()
        when {
            props.name.contentEquals(cmdName) -> {
                return true
            }
            props.aliases.isNotEmpty() -> {
                val aliases = props.aliases.parallelStream().filter { it!!.contentEquals(cmdName) }.findFirst()
                if (aliases.isPresent) {
                    return true
                }
            }
            else -> {
                return false
            }
        }
        return false
    }

    private fun isCommandNameEquals(cmd: ConsoleCommand, cmdName: String): Boolean {
        val props = cmd.getProps()

        when {
            props.name.contentEquals(cmdName) -> {
                return true
            }
            props.aliases.isNotEmpty() -> {
                val name = props.aliases.parallelStream().filter { alias -> alias!!.contentEquals(cmdName) }.findFirst()
                if (name.isPresent) {
                    return true
                }
            }
            else -> {
                return false
            }
        }
        return false
    }

    fun MessageChain.doFilter(): MessageChain {
        if (BotVariables.cfg.filterWords.isNullOrEmpty()) {
            return this
        }

        val revampChain = LinkedList<SingleMessage>()
        this.forEach { revampChain.add(it) }

        var count = 0

        for (i in revampChain.indices) {
            if (revampChain[i] is PlainText) {
                var context = revampChain[i].content
                BotVariables.cfg.filterWords.forEach {
                    if (context.contains(it)) {
                        count++
                        context = context.replace(it.toRegex(), " ")
                    }

                    if (count > 5) {
                        return EmptyMessageChain
                    }
                }
                revampChain[i] = PlainText(context)
            }
        }

        return revampChain.toMessageChain()
    }

    fun countCommands(): Int = commands.size + consoleCommands.size

    fun getCommands() = commands

    /**
     * 判断指定QQ号是否可以执行命令
     * (可以自定义命令冷却时间)
     *
     * @author Nameless
     * @param user 检测的用户
     * @param props 命令配置
     *
     * @return 目标QQ号是否处于冷却状态
     */
    private fun validateStatus(user: BotUser, props: CommandProps): Boolean {
        val currentTime = System.currentTimeMillis()

        if (user.id == 80000000L) {
            return false
        }

        if (user.isBotOwner()) {
            return true
        }

        when (props.consumerType) {
            CommandExecuteConsumerType.COOLDOWN -> {
                return when (user.lastExecuteTime) {
                    -1L -> {
                        user.lastExecuteTime = currentTime
                        true
                    }
                    else -> {
                        val result = currentTime - user.lastExecuteTime < props.consumePoint * 1000
                        user.lastExecuteTime = currentTime
                        result
                    }
                }
            }
            CommandExecuteConsumerType.POINT -> {
                return if (user.checkInPoint >= props.consumePoint) {
                    user.checkInPoint -= props.consumePoint
                    true
                } else {
                    false
                }
            }
            CommandExecuteConsumerType.COMMAND_TIME -> {
                return if (user.commandTime >= props.consumePoint) {
                    user.decreaseTime(props.consumePoint)
                    true
                } else {
                    false
                }
            }
            else -> return false
        }
    }

    data class ExecutedResult(val msg: MessageChain, val cmd: ChatCommand?, val status: CommandStatus = CommandStatus.NotACommand())

    sealed class CommandStatus(val name: String) {
        class Success: CommandStatus("成功")
        class NoPermission: CommandStatus("无权限")
        class Failed: CommandStatus("失败")
        class Disabled: CommandStatus("命令被禁用")
        class MoveToSession: CommandStatus("移交会话处理")
        class NotACommand: CommandStatus("非命令")

        fun isOk(): Boolean = this is Success || this is NoPermission || this is Failed
    }
}