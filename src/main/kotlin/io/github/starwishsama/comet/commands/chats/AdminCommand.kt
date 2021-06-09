/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats


import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.service.command.AdminService.addPermission
import io.github.starwishsama.comet.service.command.AdminService.giveCommandTime
import io.github.starwishsama.comet.service.command.AdminService.listPermissions
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain


@Suppress("SpellCheckingInspection")
class AdminCommand : ChatCommand, UnDisableableCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        return if (args.isEmpty()) {
            getHelp().toChain()
        } else {
            when (args[0]) {
                "help", "帮助" -> getHelp().toChain()
                "permlist", "权限列表", "qxlb" -> listPermissions(user, args, event)
                "permadd", "添加权限", "tjqx" -> addPermission(user, args, event)
                "give", "增加次数" -> giveCommandTime(event, args)
                else -> "命令不存在, 使用 /admin help 查看更多".toChain()
            }
        }
    }

    override fun getProps(): CommandProps =
        CommandProps("admin", arrayListOf("管理", "管", "gl"), "机器人管理员命令", "nbot.commands.admin", UserLevel.ADMIN)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /admin help 展示此帮助列表
        /admin permlist [用户] 查看用户拥有的权限
        /admin permadd [用户] [权限名] 给一个用户添加权限
        /admin give [用户] [命令条数] 给一个用户添加命令条数
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        val bLevel = getProps().level
        if (user.compareLevel(bLevel)) return true
        if (e is GroupMessageEvent && e.sender.permission != MemberPermission.MEMBER) return true
        return false
    }
}