package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.util.*

@CometCommand
class GroupConfigCommand : ChatCommand, UnDisableableCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(user.id) && event is GroupMessageEvent) {
            if (args.isNotEmpty()) {
                val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
                when (args[0].toLowerCase(Locale.ROOT)) {
                    "helper", "群管" -> {
                        return if (args.size == 2) {
                            val target = CometUtil.parseAtAsBotUser(event, args[1])
                            if (target != null) {
                                if (cfg.isHelper(target.id)) {
                                    cfg.removeHelper(target.id)
                                    sendMessage("成功将 ${target.id} 移出群助手列表")
                                } else {
                                    cfg.addHelper(target.id)
                                    sendMessage("成功将 ${target.id} 移出群助手列表")
                                }
                            } else {
                                sendMessage("找不到你想要添加/删除的用户")
                            }
                        } else {
                            sendMessage(getHelp())
                        }
                    }
                    "repeat" -> {
                        cfg.doRepeat = !cfg.doRepeat
                        return sendMessage("已${if (cfg.doRepeat) "开启" else "关闭"}群复读机")
                    }
                    "autojoin" -> {
                        return if (event.group.botPermission == MemberPermission.MEMBER) {
                            "抱歉, 机器人不是群管, 无法自动接受加群请求.".sendMessage()
                        } else {
                            cfg.autoAccept = !cfg.autoAccept
                            "已${if (cfg.autoAccept) "开启" else "关闭"}自动接受加群请求".sendMessage()
                        }
                    }
                    "function", "fun", "func" -> {
                        if (args.size < 2) {
                            return sendMessage("""
                现在支持禁用彗星 Bot 的命令功能了!
                /gs function [命令名] 在本群禁用指定命令
            """.trimIndent())
                        }

                        return cfg.disableCommand(args[1]).msg.sendMessage()
                    }
                    "autoreply", "ar", "自动回复", "关键词", "keyword", "kw" -> {
                        if (args.size == 1) {
                            return "/group ar [关键词] [回复内容]".sendMessage()
                        } else {
                            val keyWord = args[2]
                            val reply = args.getRestString(3)

                            cfg.keyWordReply.forEach {
                                if (it.reply.text == reply) {
                                    it.keyWords.add(keyWord)
                                    return "已发现现有配置, 成功添加关键词".sendMessage()
                                }
                            }

                            return if (cfg.keyWordReply.add(PerGroupConfig.ReplyKeyWord(
                                    mutableListOf(keyWord),
                                    mutableListOf(),
                                    MessageWrapper(reply)
                                ))) {
                                "添加关键词成功".sendMessage()
                            } else {
                                "添加关键词失败".sendMessage()
                            }
                        }
                    }
                }
            } else {
                return getHelp().sendMessage()
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