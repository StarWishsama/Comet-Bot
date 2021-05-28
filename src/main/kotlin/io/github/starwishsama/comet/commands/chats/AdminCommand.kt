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


import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain


@Suppress("SpellCheckingInspection")
class AdminCommand : ChatCommand, UnDisableableCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        return if (args.isEmpty()) {
            "命令不存在, 使用 /admin help 查看更多".toChain()
        } else {
            when (args[0]) {
                "help", "帮助" -> getHelp().convertToChain()
                "permlist", "权限列表", "qxlb" -> permList(user, args, event)
                "permadd", "添加权限", "tjqx" -> permAdd(user, args, event)
                "give", "增加次数" -> giveCommandUseTime(event, args)
                else -> "命令不存在, 使用 /admin help 查看更多".toChain()
            }
        }
    }

    override fun getProps(): CommandProps =
        CommandProps("admin", arrayListOf("管理", "管", "gl"), "机器人管理员命令", "nbot.commands.admin", UserLevel.ADMIN)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /admin dk (开始时间) [结束时间] 创建一个打卡
        /admin dksj 查看最近一次打卡的数据
        /admin permadd [用户] [权限名] 给一个用户添加权限
        /admin give [用户] [命令条数] 给一个用户添加命令条数
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        val bLevel = getProps().level
        if (user.compareLevel(bLevel)) return true
        if (e is GroupMessageEvent && e.sender.permission != MemberPermission.MEMBER) return true
        return false
    }

    private fun permList(user: BotUser, args: List<String>, event: MessageEvent): MessageChain {
        return if (args.size > 1) {
            val target: BotUser? = CometUtil.parseAtAsBotUser(event, args[1])
            val permission = target?.getPermissions()
            if (permission != null) {
                toChain(permission)
            } else {
                toChain("该用户没有任何权限")
            }
        } else {
            toChain(user.getPermissions())
        }
    }

    private fun permAdd(user: BotUser, args: List<String>, event: MessageEvent): MessageChain {
        if (user.isBotOwner()) {
            if (args.size > 1) {
                val target: BotUser? = CometUtil.parseAtAsBotUser(event, args[1])

                val validate =
                    CommandExecutor.getCommands().parallelStream().filter { it.getProps().permission == args[2] }
                        .findAny().isPresent

                return if (validate) {
                    target?.addPermission(args[2])
                    toChain("添加权限成功")
                } else {
                    "找不到 ${args[2]} 对应的命令".toChain()
                }
            }
        } else {
            return toChain("你没有权限")
        }
        return EmptyMessageChain
    }

    private fun giveCommandUseTime(event: MessageEvent, args: List<String>): MessageChain {
        if (args.size > 1) {
            val target: BotUser? = CometUtil.parseAtAsBotUser(event, args[1])

            return if (target != null) {
                if (args[2].toInt() <= 1000000) {
                    target.addTime(args[2].toInt())
                    toChain("成功为 $target 添加 ${args[2]} 次命令条数")
                } else {
                    toChain("给予的次数超过系统限制上限")
                }
            } else {
                toChain("找不到此用户")
            }
        }
        return EmptyMessageChain
    }
}