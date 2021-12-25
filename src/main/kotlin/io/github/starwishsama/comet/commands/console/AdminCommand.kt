/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.file.DataSetup
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandOwner

import java.util.*

object AdminCommand : CompositeCommand(
    ConsoleCommandOwner, "admin",
    description = """
        /admin upgrade [ID] (权限组名) 修改权限组
        /admin reload 重载配置文件
        /admin rp [硬币] 重置所有账号的硬币为指定硬币数
        /admin cmd [群号] 在指定群禁用命令
    """.trimIndent()
) {

    @SubCommand
    suspend fun CommandSender.upgrade(target: Long) {
        val targetUser = CometUser.getUser(target)

        if (targetUser == null) {
            sendMessage("目标没有使用过 Comet")
            return
        }

        val targetLevel = targetUser.level.ordinal + 1

        if (targetLevel >= UserLevel.values().size) {
            targetUser.level = UserLevel.USER
        } else {
            targetUser.level = UserLevel.values()[targetLevel]
        }

        sendMessage("成功将 ${targetUser.id} 设为 ${targetUser.level.name}")
    }

    @SubCommand
    suspend fun CommandSender.upgrade(target: Long, level: String) {
        val targetUser = CometUser.getUser(target)

        if (targetUser == null) {
            sendMessage("目标没有使用过 Comet")
            return
        }

        runCatching {
            UserLevel.valueOf(level.uppercase(Locale.getDefault()))
        }.onSuccess {
            targetUser.level = it
            sendMessage("成功将 ${targetUser.id} 设为 ${targetUser.level.name}")
        }.onFailure {
            sendMessage("无效的权限组名")
        }
    }

    @SubCommand
    suspend fun CommandSender.reload() {
        DataSetup.reload()
        sendMessage("配置文件已重载")
    }

    @SubCommand
    suspend fun CommandSender.cmd(groupId: Long, cmdName: String) {
        val cmd = CommandManager.getCommand(cmdName)

        if (cmd == null) {
            sendMessage("无效的命令名")
        } else {
            sendMessage(cmd.props.disableCommand(groupId).msg)
        }
    }

    @SubCommand
    suspend fun CommandSender.groups() {
        sendMessage(buildString {
            append("已加入的群聊:\n")
            CometVariables.comet.getBot().groups.forEach {
                append("${it.name} (${it.id}),")
            }
        }.removeSuffix(",").trim())
    }
}