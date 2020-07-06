package io.github.starwishsama.nbot.commands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.commands.interfaces.ConsoleCommand
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.MessageHolder
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.utils.BotUtil
import io.github.starwishsama.nbot.utils.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.util.*

/**
 * 无名 Bot 命令处理器
 * 处理群聊/私聊聊天信息中存在的命令
 * @author Nameless
 */
object CommandExecutor {
    var commands: List<UniversalCommand> = mutableListOf()

    /**
     * 注册命令
     *
     * @param command 要注册的命令
     */
    fun setupCommand(command: UniversalCommand) {
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

    /**
     * 执行消息中的命令
     *
     * @param command 纯文本命令 (后台)
     * @param event Mirai 消息命令 (聊天)
     */
    suspend fun execute(command: String, event: MessageEvent?): MessageHolder {
        val senderId = event?.sender?.id ?: -1
        val message = event?.message?.contentToString() ?: command
        try {
            if (isCommandPrefix(message) && !SessionManager.isValidSessionById(senderId)) {
                val cmd = getCommand(getCommandName(message))
                if (cmd != null) {
                    val splitMessage = message.split(" ")
                    val splitCommand = splitMessage.subList(1, splitMessage.size)
                    if (cmd is ConsoleCommand) {
                        val result = cmd.execute(splitCommand)
                        return MessageHolder(result, null)
                    } else if (event != null) {
                        BotMain.logger.debug("[命令] " + senderId + " 执行了命令: " + cmd.getProps().name)
                        var user = BotUser.getUser(senderId)
                        if (user == null) {
                            user = BotUser.quickRegister(senderId)
                        }

                        return if (user.compareLevel(cmd.getProps().level) || user.hasPermission(cmd.getProps().permission)) {
                            MessageHolder("", doFilter(cmd.execute(event, splitMessage.subList(1, splitMessage.size), user)))
                        } else {
                            MessageHolder("", BotUtil.sendMsgPrefix("你没有权限!").toMirai())
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            BotMain.logger.warning("[命令] 在试图执行命令时发生了一个错误, 原文: $message, 发送者: $senderId", t)
        }
        return MessageHolder("", EmptyMessageChain)
    }

    private fun getCommand(cmdPrefix: String): UniversalCommand? {
        commands.forEach {
            if (commandEquals(it, cmdPrefix)) {
                return it
            }
        }
        return null
    }

    private fun getCommandName(command: String): String {
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

    private fun doFilter(chain: MessageChain) : MessageChain {
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