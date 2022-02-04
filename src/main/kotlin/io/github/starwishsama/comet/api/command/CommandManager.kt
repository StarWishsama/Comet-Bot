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

/**
 * 彗星 Bot 命令管理器
 *
 * @author StarWishsama
 */
object CommandManager {
    private val commands: MutableSet<ChatCommand> = mutableSetOf()

    /**
     * 注册命令
     *
     * @param command 要注册的命令
     */
    private fun setupCommand(command: ChatCommand) {
        if (command.canRegister() && !commands.add(command)) {
            CometVariables.logger.warning("[命令] 正在尝试注册已有命令 ${command.props.name}")
        }
    }

    /**
     * 注册命令
     *
     * @param commands 要注册的命令集合
     */
    @Suppress("unused")
    fun setupCommands(commands: Array<ChatCommand>) {
        commands.forEach {
            if (!CommandManager.commands.contains(it) && it.canRegister()) {
                CommandManager.commands.add(it)
            }
        }
    }

    fun getCommand(cmdPrefix: String): ChatCommand? {
        return commands.find { isCommandNameEquals(it, cmdPrefix) }
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
        val props = cmd.props
        when {
            props.name.contentEquals(cmdName) -> {
                return true
            }
            props.aliases.isNotEmpty() -> {
                val aliases = props.aliases.find { it.contentEquals(cmdName) }
                if (aliases != null) {
                    return true
                }
            }
            else -> {
                return false
            }
        }
        return false
    }

    fun countCommands(): Int = commands.size

    fun getCommands() = commands
}