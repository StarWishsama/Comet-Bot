package io.github.starwishsama.comet.api.command

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.api.command.interfaces.SuspendCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.DaemonSession
import io.github.starwishsama.comet.sessions.SessionGetResult
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import io.github.starwishsama.comet.utils.debugS
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.*
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

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


    /**
     * FIXME: 考虑多个 Comet 机器人在群内的问题, 优先处理原则
     */
    @ExperimentalTime
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
                        BotVariables.logger.debugS(
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
    @ExperimentalTime
    suspend fun dispatchCommand(event: MessageEvent): ExecutedResult {
        val executedTime = LocalDateTime.now()
        val senderId = event.sender.id
        val message = event.message.contentToString()

        val cmd = getCommand(getCommandName(message))

        val debug = cmd?.getProps()?.name?.contentEquals("debug") == true

        val user = BotUser.getUserSafely(senderId)

        if (BotVariables.switch || debug) {
            try {
                val session = SessionManager.getSessionByEvent(event)

                /**
                 * 如果不是监听会话, 则停止尝试解析并执行可能的命令
                 * 反之在监听时仍然可以执行命令
                 */
                if (session.exists() && !handleSession(event, executedTime)) {
                    return ExecutedResult(EmptyMessageChain, cmd, CommandStatus.MoveToSession())
                }

                /**
                 * 检查是否在尝试执行被禁用命令
                 */
                if (cmd != null && event is GroupMessageEvent &&
                        GroupConfigManager.getConfig(event.group.id)?.isDisabledCommand(cmd) == true) {
                    return if (BotUtil.hasNoCoolDown(user.id)) {
                        ExecutedResult(BotUtil.sendMessage("该命令已被管理员禁用"), cmd, CommandStatus.Disabled())
                    } else {
                        ExecutedResult(EmptyMessageChain, cmd, CommandStatus.Disabled())
                    }
                }

                val prefix = isCommandPrefix(message)

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
                        BotUtil.sendMessage("你没有权限!")
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
                        ExecutedResult(BotUtil.sendMessage("在试图执行命令时发生了一个错误\n简易报错信息 (如果有的话):\n${t.javaClass.name}: ${t.message?.limitStringSize(30)}"), cmd, CommandStatus.Failed())
                    } else {
                        ExecutedResult(BotUtil.sendMessage("在试图执行命令时发生了一个错误, 请联系管理员"), cmd, CommandStatus.Failed())
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

    /**
     * 处理会话
     * @return 是否为监听会话
     */
    @ExperimentalTime
    private suspend fun handleSession(event: MessageEvent, time: LocalDateTime): Boolean {
        val sender = event.sender

        if (isCommandPrefix(event.message.contentToString()).isEmpty()) {
            val session: SessionGetResult = SessionManager.getSessionByEvent(event)
            if (session.exists()) {
                val hasDaemonSession = session.sessionList.parallelStream().filter { it is DaemonSession }.findAny()
                hasDaemonSession.ifPresent {
                    GlobalScope.run {
                        session.sessionList.forEach { current ->
                            if (current is DaemonSession) {
                                return@forEach
                            }

                            val command = current.command
                            if (command is SuspendCommand) {
                                runBlocking { command.handleInput(event, BotUser.getUserSafely(sender.id), current) }
                            }
                        }
                    }
                }

                return hasDaemonSession.isPresent
            }
        }

        BotVariables.logger.debug(
                "[会话] 处理会话耗时 ${time.getLastingTimeAsString(unit = TimeUnit.SECONDS, msMode = true)}"
        )
        return true
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
        val cmdPrefix = isCommandPrefix(message)

        val index = message.indexOf(cmdPrefix) + cmdPrefix.length

        return message.substring(index, message.length).split(" ")[0]
    }

    /**
     * 消息开头是否为命令前缀
     *
     * @return 消息前缀, 不匹配返回空
     */
    private fun isCommandPrefix(message: String): String {
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

        return revampChain.asMessageChain()
    }

    fun countCommands(): Int = commands.size + consoleCommands.size

    fun getCommands() = commands

    data class ExecutedResult(val msg: MessageChain, val cmd: ChatCommand?, val status: CommandStatus = CommandStatus.NotACommand())

    sealed class CommandStatus(val name: String) {
        class Success: CommandStatus("成功")
        class NoPermission: CommandStatus("无权限")
        class Failed: CommandStatus("失败")
        class Disabled: CommandStatus("命令被禁用")
        class MoveToSession: CommandStatus("移交会话处理")
        class NotACommand: CommandStatus("非命令")

        fun isOk(): Boolean = this is Success || this is NoPermission
    }
}