/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.command

import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

object AdminService {
    private const val maxCommandTime = 1000000

    fun listPermissions(user: CometUser, id: String, messageChain: MessageChain): MessageChain {
        return if (id.isNotEmpty()) {
            val target: CometUser? = CometUtil.parseAtAsBotUser(messageChain, id)
            val permissions = target?.getPermissions()
            if (permissions != null) {
                "该用户拥有的权限: $permissions".toMessageChain()
            } else {
                toMessageChain("该用户没有任何权限")
            }
        } else {
            "你拥有的权限: ${user.getPermissions()}".toMessageChain()
        }
    }

    fun addPermission(user: CometUser, id: String, nodeName: String, messageChain: MessageChain): MessageChain {
        if (user.isBotOwner()) {
            if (id.isNotBlank()) {
                val target: CometUser? = CometUtil.parseAtAsBotUser(messageChain, id)

                val targetNode = CommandManager.getCommands().find { it.props.permissionNodeName == nodeName }?.props?.permissionNodeName

                return if (targetNode?.isNotEmpty() == true) {
                    target?.addPermission(targetNode)
                    "添加权限成功".toMessageChain()
                } else {
                    "找不到 $nodeName 对应的命令".toMessageChain()
                }
            }
        } else {
            return toMessageChain("你没有权限")
        }
        return EmptyMessageChain
    }

    fun removePermission(user: CometUser, id: String, nodeName: String, messageChain: MessageChain): MessageChain {
        if (user.isBotOwner()) {
            if (id.isNotBlank()) {
                val target: CometUser? = CometUtil.parseAtAsBotUser(messageChain, id)

                val targetPermission =
                    CommandManager.getCommands().find { it.props.permissionNodeName == nodeName }?.props?.permissionNodeName

                return if (targetPermission?.isNotBlank() == true) {
                    target?.removePermission(targetPermission)
                    toMessageChain("删除权限成功")
                } else {
                    "找不到 $nodeName 对应的命令".toMessageChain()
                }
            }
        } else {
            return toMessageChain("你没有权限")
        }
        return EmptyMessageChain
    }

    fun giveCommandTime(event: MessageEvent, args: List<String>): MessageChain {
        if (args.size > 1) {
            val target: CometUser = CometUtil.parseAtAsBotUser(event.message, args[1]) ?: return "找不到此用户".toMessageChain()

            val commandTime = args[2].toIntOrNull() ?: return "请输入正确的数字!".toMessageChain()


            if (commandTime <= maxCommandTime) {
                target.addPoint(commandTime)
                "成功为 $target 添加 $commandTime 点硬币".toMessageChain()
            } else {
                "给予的次数超过系统限制上限".toMessageChain()
            }
        }
        return EmptyMessageChain
    }
}