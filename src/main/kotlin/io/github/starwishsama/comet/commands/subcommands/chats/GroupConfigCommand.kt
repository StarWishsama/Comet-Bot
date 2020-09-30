package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.annotations.CometCommand
import io.github.starwishsama.comet.commands.CommandExecutor
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.util.*

@CometCommand
class GroupConfigCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(user.id) && event is GroupMessageEvent) {
            if (args.isNotEmpty()) {
                val cfg = GroupConfigManager.getConfigSafely(event.group.id)
                when (args[0].toLowerCase(Locale.ROOT)) {
                    "helper", "群管" -> {
                        return if (args.size == 2) {
                            val target = BotUtil.getAt(event, args[1])
                            if (target != null) {
                                if (cfg.isHelper(target.id)) {
                                    cfg.removeHelper(target.id)
                                    BotUtil.sendMessage("成功将 ${target.id} 移出群助手列表")
                                } else {
                                    cfg.addHelper(target.id)
                                    BotUtil.sendMessage("成功将 ${target.id} 移出群助手列表")
                                }
                            } else {
                                BotUtil.sendMessage("找不到你想要添加/删除的用户")
                            }
                        } else {
                            BotUtil.sendMessage(getHelp())
                        }
                    }
                    "repeat" -> {
                        cfg.doRepeat = !cfg.doRepeat
                        return BotUtil.sendMessage("已${if (cfg.doRepeat) "开启" else "关闭"}群复读机")
                    }
                    "autojoin" -> {
                        return if (event.group.botPermission == MemberPermission.MEMBER) {
                            BotUtil.sendMessage("抱歉, 机器人不是群管, 无法自动接受加群请求.")
                        } else {
                            cfg.autoAccept = !cfg.autoAccept
                            BotUtil.sendMessage("已${if (cfg.autoAccept) "开启" else "关闭"}自动接受加群请求")
                        }
                    }
                    "function", "fun", "func" -> {
                        if (args.size < 2) {
                            return BotUtil.sendMessage("""
                现在支持禁用彗星 Bot 的命令功能了!
                /gs function [命令名] 在本群禁用指定命令
            """.trimIndent())
                        }

                        val command = CommandExecutor.getCommand(args[1])
                        return if (command != null) {
                            if (!cfg.disabledCommands.contains(command)) {
                                cfg.disabledCommands.add(command)
                                BotUtil.sendMessage("成功禁用该命令")
                            } else {
                                cfg.disabledCommands.remove(command)
                                BotUtil.sendMessage("成功启用该命令")
                            }
                        } else {
                            BotUtil.sendMessage("该命令不存在!")
                        }
                    }
                }
            } else {
                return BotUtil.sendMessage(getHelp())
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("group", arrayListOf("群设置", "gs"), "设置群内设置", "nbot.commands.groupconfig", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /group helper [@/QQ] 添加/删除群助理
        /group repeat 开启/关闭本群机器人复读功能
        /group autojoin 开启/关闭本群机器人自动接受加群请求
        /group func 启用/禁用本群可使用的命令
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        val level = getProps().level
        if (user.compareLevel(level)) return true
        if (e is GroupMessageEvent && e.sender.permission > MemberPermission.MEMBER) return true
        return false
    }
}