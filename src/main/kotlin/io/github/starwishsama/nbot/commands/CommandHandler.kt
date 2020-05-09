package io.github.starwishsama.nbot.commands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.util.*

/**
 * Mirai 命令处理器
 * 处理群聊/私聊聊天信息中存在的命令
 * @author Nameless
 */
object CommandHandler {
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
     * @param event 消息
     */
    suspend fun execute(event: MessageEvent): MessageChain {
        if (isCommandPrefix(event.message.content) && !SessionManager.isValidSession(event.sender.id)) {
            val cmd = getCommand(getCommandName(event.message.contentToString()))
            if (cmd != null) {
                BotInstance.logger.debug("[命令] " + event.sender.id + " 执行了命令: " + cmd.getProps().name)
                var user = BotUser.getUser(event.sender.id)
                if (user == null) {
                    user = BotUser.quickRegister(event.sender.id)
                }

                return if (user.compareLevel(cmd.getProps().level) || user.hasPermission(cmd.getProps().permission)) {
                    val splitMessage = event.message.contentToString().split(" ")
                    doFilter(cmd.execute(event, splitMessage.subList(1, splitMessage.size), user))
                } else {
                    BotUtil.sendMsgPrefix("你没有权限!").toMirai()
                }
            }
        }
        return EmptyMessageChain
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
        return BotConstants.cfg.commandPrefix.contains(
                message.substring(0, 1)
        ) && message.isNotEmpty()
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
                        context = context.replace(it.toRegex(), "")
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