package io.github.starwishsama.comet.commands

import io.github.starwishsama.comet.BotConstants
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.commands.interfaces.ConsoleCommand
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * 无名 Bot 命令处理器
 * 处理群聊/私聊聊天信息中存在的命令
 * @author Nameless
 */
object CommandExecutor {
    var commands: List<UniversalCommand> = mutableListOf()
    private var consoleCommands = mutableListOf<ConsoleCommand>()

    /**
     * 注册命令
     *
     * @param command 要注册的命令
     */
    private fun setupCommand(command: UniversalCommand) {
        if (!commands.contains(command)) {
            commands = commands + command
        }
    }

    /**
     * 注册命令
     *
     * @param commands 要注册的命令集合
     */
    fun setupCommand(commands: Array<UniversalCommand>) {
        commands.forEach {
            if (!this.commands.contains(it)) {
                this.commands = this.commands.plus(it)
            }
        }
    }

    fun setupCommand(commands: Array<Any>) {
        commands.forEach {
            when (it) {
                is UniversalCommand -> setupCommand(it)
                is ConsoleCommand -> consoleCommands.plusAssign(it)
                else -> {
                    Comet.logger.warning("[命令] 正在尝试注册非命令类 ${it.javaClass.simpleName}")
                }
            }
        }
    }

    /**
     * 执行消息中的命令
     *
     * @param event Mirai 消息命令 (聊天)
     */
    suspend fun execute(event: MessageEvent): MessageChain {
        val executedTime = LocalDateTime.now()
        val senderId = event.sender.id
        val message = event.message.contentToString()
        try {
            if (isCommandPrefix(message) && !SessionManager.isValidSessionById(senderId)) {
                val cmd = getCommand(getCommandName(message))
                if (cmd != null) {
                    val splitMessage = message.split(" ")
                    Comet.logger.debug("[命令] ${if (senderId != -1L) senderId.toString() else "后台"} 尝试执行命令: " + cmd.getProps().name)
                    var user = BotUser.getUser(senderId)
                    if (user == null) {
                        user = BotUser.quickRegister(senderId)
                    }

                    val result: MessageChain =
                            if (user.compareLevel(cmd.getProps().level) || user.hasPermission(cmd.getProps().permission)) {
                                doFilter(cmd.execute(event, splitMessage.subList(1, splitMessage.size), user))
                            } else {
                                BotUtil.sendMsgPrefix("你没有权限!").toMirai()
                            }

                    val usedTime = Duration.between(executedTime, LocalDateTime.now())
                    Comet.logger.debug("[命令] 命令执行耗时 ${usedTime.toSecondsPart()}s${usedTime.toMillisPart()}ms")

                    return result
                }
            }
        } catch (t: Throwable) {
            Comet.logger.warning("[命令] 在试图执行命令时发生了一个错误, 原文: $message, 发送者: $senderId", t)
            return "Bot > 在试图执行命令时发生了一个错误, 请联系管理员".toMirai()
        }
        return EmptyMessageChain
    }

    /**
     * 执行后台命令
     *
     * @param content 纯文本命令 (后台)
     */
    suspend fun executeConsole(content: String): String {
        try {
            if (isCommandPrefix(content)) {
                val cmd = getConsoleCommand(getCommandName(content))
                if (cmd != null) {
                    val splitMessage = content.split(" ")
                    val splitCommand = splitMessage.subList(1, splitMessage.size)
                    Comet.logger.debug("[命令] 后台尝试执行命令: " + cmd.getProps().name)
                    return cmd.execute(splitCommand)
                }
            }
        } catch (t: Throwable) {
            Comet.logger.warning("[命令] 在试图执行命令时发生了一个错误, 原文: $content", t)
            return ""
        }
        return ""
    }

    private fun getCommand(cmdPrefix: String): UniversalCommand? {
        for (command in commands) {
            if (commandEquals(command, cmdPrefix)) {
                return command
            }
        }
        return null
    }

    private fun getConsoleCommand(cmdPrefix: String): ConsoleCommand? {
        for (command in consoleCommands) {
            if (commandEquals(command, cmdPrefix)) {
                return command
            }
        }
        return null
    }

    fun getCommandName(command: String): String {
        var cmdPrefix = command
        for (string: String in BotConstants.cfg.commandPrefix) {
            cmdPrefix = cmdPrefix.replace(string, "")
        }

        return cmdPrefix.split(" ")[0]
    }

    private fun isCommandPrefix(message: String): Boolean {
        return message.isNotEmpty() && BotConstants.cfg.commandPrefix.contains(message.substring(0, 1))
    }

    private fun commandEquals(cmd: UniversalCommand, cmdName: String): Boolean {
        val props = cmd.getProps()
        when {
            props.name.contentEquals(cmdName) -> {
                return true
            }
            props.aliases != null -> {
                props.aliases?.forEach {
                    if (it.contentEquals(cmdName)) {
                        return true
                    }
                }
            }
            else -> {
                return false
            }
        }
        return false
    }

    private fun commandEquals(cmd: ConsoleCommand, cmdName: String): Boolean {
        val props = cmd.getProps()
        when {
            props.name.contentEquals(cmdName) -> {
                return true
            }
            props.aliases != null -> {
                props.aliases?.forEach {
                    if (it.contentEquals(cmdName)) {
                        return true
                    }
                }
            }
            else -> {
                return false
            }
        }
        return false
    }

    private fun doFilter(chain: MessageChain): MessageChain {
        if (BotConstants.cfg.filterWords.isNullOrEmpty()) {
            return chain
        }

        val revampChain = LinkedList<SingleMessage>()
        chain.forEach { revampChain.add(it) }

        var count = 0

        for (i in revampChain.indices) {
            if (revampChain[i] is PlainText) {
                var context = revampChain[i].content
                BotConstants.cfg.filterWords.forEach {
                    if (context.contains(it)) {
                        count++
                        context = context.replace(it.toRegex(), " ")
                    }

                    if (count > 3) {
                        return EmptyMessageChain
                    }
                }
                revampChain[i] = PlainText(context)
            }
        }

        return revampChain.asMessageChain()
    }
}