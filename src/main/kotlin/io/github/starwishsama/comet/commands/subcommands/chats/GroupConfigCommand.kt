package io.github.starwishsama.comet.commands.subcommands.chats

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

class GroupConfigCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.id) && event is GroupMessageEvent && hasPermission(user, event.sender.permission)) {
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
                        val result = !cfg.doRepeat
                        return BotUtil.sendMessage("已${if (result) "开启" else "关闭"}群复读机")
                    }
                    "autojoin" -> {
                        return if (event.group.botPermission == MemberPermission.MEMBER) {
                            BotUtil.sendMessage("抱歉, 机器人不是群管, 无法自动接受加群请求.")
                        } else {
                            val result = !cfg.autoAccept
                            BotUtil.sendMessage("已${if (result) "开启" else "关闭"}自动接受加群请求")
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
    """.trimIndent()

    private fun hasPermission(user: BotUser, permission: MemberPermission): Boolean {
        return user.isBotOwner() || permission != MemberPermission.OWNER
    }
}