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
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand

/**
 * 彗星 Bot 命令管理器
 *
 * @author StarWishsama
 */
object CommandManager {
    private val commands: MutableSet<ChatCommand> = mutableSetOf()
    private val consoleCommands = mutableListOf<ConsoleCommand>()

    /**
     * 注册命令
     *
     * @param command 要注册的命令
     */
    private fun setupCommand(command: ChatCommand) {
        if (command.canRegister() && !commands.add(command)) {
            CometVariables.logger.warning("[命令] 正在尝试注册已有命令 ${command.getProps().name}")
        }
    }

    private fun setupConsoleCommand(command: ConsoleCommand) {
        if (!consoleCommands.contains(command)) {
            consoleCommands.plusAssign(command)
        } else {
            CometVariables.logger.warning("[命令] 正在尝试注册已有后台命令 ${command.getProps().name}")
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
            if (!CommandManager.commands.contains(it) && it.canRegister()) {
                CommandManager.commands.add(it)
            }
        }
    }

    fun setupCommand(commands: Array<Any>) {
        commands.forEach {
            when (it) {
                is ChatCommand -> setupCommand(it)
                is ConsoleCommand -> setupConsoleCommand(it)
                else -> {
                    CometVariables.logger.warning("[命令] 正在尝试注册非命令类 ${it.javaClass.simpleName}")
                }
            }
        }
    }

    fun getCommand(cmdPrefix: String): ChatCommand? {
        val command = commands.parallelStream().filter {
            isCommandNameEquals(it, cmdPrefix)
        }.findFirst()

        return if (command.isPresent) command.get() else null
    }

    fun getConsoleCommand(cmdPrefix: String): ConsoleCommand? {
        for (command in consoleCommands) {
            if (isCommandNameEquals(command, cmdPrefix)) {
                return command
            }
        }
        return null
    }

    fun getCommandName(message: String): String {
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
            CometVariables.cfg.commandPrefix.forEach {
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

    fun countCommands(): Int = commands.size + consoleCommands.size

    fun getCommands() = commands
}