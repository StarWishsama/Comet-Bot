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


import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.toMessageWrapper
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText


class GroupConfigCommand : ChatCommand, UnDisableableCommand {
    // TODO 适配私聊设置
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (event is GroupMessageEvent) {
            if (args.isNotEmpty()) {
                val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
                when (args[0].lowercase()) {
                    "helper", "群管" -> {
                        return if (args.size == 2) {
                            val target = CometUtil.parseAtAsBotUser(event, args[1])
                            if (target != null) {
                                if (cfg.isHelper(target.id)) {
                                    cfg.removeHelper(target.id)
                                    toChain("成功将 ${target.id} 移出群助手列表")
                                } else {
                                    cfg.addHelper(target.id)
                                    toChain("成功将 ${target.id} 加入群助手列表")
                                }
                            } else {
                                toChain("找不到你想要添加/删除的用户")
                            }
                        } else {
                            toChain(getHelp())
                        }
                    }
                    "repeat" -> {
                        cfg.canRepeat = !cfg.canRepeat
                        return toChain("已${if (cfg.canRepeat) "开启" else "关闭"}群复读机")
                    }
                    "autojoin" -> {
                        if (args.size > 1) {
                            return when (args[1]) {
                                "condition", "条件", "tj", "cd" -> {
                                    if (args.size > 2) {
                                        cfg.autoAcceptCondition = args.getRestString(2)
                                        "成功设置自动通过申请条件!".toChain()
                                    } else {
                                        "/gs autojoin condition [关键词]\n满足关键词的入群申请会自动通过".toChain()
                                    }
                                }
                                else -> {
                                    "/gs autojoin condition [关键词]\n满足关键词的入群申请会自动通过".toChain()
                                }
                            }
                        } else {
                            return if (event.group.botPermission == MemberPermission.MEMBER) {
                                "抱歉, 机器人不是群管, 无法自动接受加群请求.".toChain()
                            } else {
                                cfg.autoAccept = !cfg.autoAccept
                                "已${if (cfg.autoAccept) "开启" else "关闭"}自动接受加群请求".toChain()
                            }
                        }
                    }
                    "function", "fun", "func" -> {
                        if (args.size < 2) {
                            return toChain(
                                """
                现在支持禁用彗星 Bot 的命令功能了!
                /gs function [命令名] 在本群禁用指定命令
            """.trimIndent()
                            )
                        }

                        val cmd = CommandManager.getCommand(args[1])

                        return cmd?.props?.disableCommand(event.group.id)?.msg?.toChain() ?: "找不到对应命令".toChain()
                    }
                    "autoreply", "ar", "自动回复", "关键词", "keyword", "kw" -> {
                        if (args.size == 1) {
                            return "/group ar [关键词] [回复内容]".toChain()
                        } else {
                            val keyWord = args[1]
                            val reply = args.getRestString(2)

                            cfg.keyWordReply.forEach {
                                if (it.reply.getAllText() == reply) {
                                    it.keyWords.add(keyWord)
                                    return "已发现现有配置, 成功添加关键词".toChain()
                                }
                            }

                            return if (cfg.keyWordReply.add(
                                    PerGroupConfig.ReplyKeyWord(
                                        mutableListOf(keyWord),
                                        MessageWrapper().addText(reply)
                                    )
                                )
                            ) {
                                "添加关键词成功".toChain()
                            } else {
                                "添加关键词失败".toChain()
                            }
                        }
                    }
                    "newcomer", "入群欢迎", "自动欢迎", "zdhy", "rqhy" -> {
                        if (args.size > 1) {
                            return if (args.size > 2) {
                                val welcomeText = MessageChainBuilder().append(args.getRestString(2)).apply {
                                    event.message.filter { it !is PlainText }.forEach { element ->
                                        append(element)
                                    }
                                }.build().toMessageWrapper()

                                cfg.newComerWelcomeText = welcomeText
                                "设置欢迎消息成功".toChain()
                            } else {
                                "/group newcomer [入群欢迎内容]\n内容支持纯文字 + 图片, 想要 @ 入群成员请用 [At] 代替.".toChain()
                            }
                        } else {
                            cfg.newComerWelcome = !cfg.newComerWelcome
                            return "已${if (cfg.newComerWelcome) "开启" else "关闭"}加群自动欢迎".toChain()
                        }
                    }
                    "fileremove", "filerm", "frm", "群文件自动删除", "文件删除" -> {
                        if (args.size > 1) {
                            when (args[1]) {
                                "switch" -> {
                                    cfg.oldFileCleanFeature = !cfg.oldFileCleanFeature
                                    return "自动删除文件功能: ${cfg.oldFileCleanFeature}".toChain()
                                }
                                "delay" -> {
                                    if (args.size > 2) {
                                        val delay = args[2].toLongOrNull() ?: return "请输入正确的数字".toChain()
                                        cfg.oldFileCleanDelay = delay
                                        return "已设置自动删除超过 $delay ms 的文件.".toChain()
                                    } else {
                                        return "/group frm delay [删除延迟]".toChain()
                                    }
                                }
                                "pattern" -> {
                                    return if (args.size > 2) {
                                        cfg.oldFileMatchPattern = args[2]
                                        "已设置文件匹配正则表达式为 [${cfg.oldFileMatchPattern}].".toChain()
                                    } else {
                                        "/group frm pattern [文件匹配正则表达式]".toChain()
                                    }
                                }
                                else -> {
                                    return """
                                        /group frm switch 开启或关闭自动删除文件
                                        /group frm pattern [文件匹配正则表达式] 设置文件匹配正则表达式
                                        /group frm delay [删除延迟] 设置文件删除延迟
                                    """.trimIndent().toChain()
                                }
                            }
                        } else {
                            return getHelp().toChain()
                        }
                    }
                    else -> {
                        return getHelp().toChain()
                    }
                }
            } else {
                return getHelp().toChain()
            }
        } else {
            return "抱歉, 该命令仅供群聊使用!".toChain()
        }
    }

    override val props: CommandProps =
        CommandProps("group", arrayListOf("群设置", "gs"), "设置群内设置", "nbot.commands.groupconfig", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /group helper [@/QQ] 添加/删除群助理
        /group repeat 开启/关闭本群机器人复读功能
        /group autojoin 开启/关闭本群机器人自动接受加群请求
        /group func 启用/禁用本群可使用的命令
        /group autoreply 设置自动回复
        /group newcomer 设置入群欢迎内容
        /group frm 设置群文件自动删除
    """.trimIndent()

    override fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
        val level = props.level
        if (user.compareLevel(level)) return true
        if (e is GroupMessageEvent && e.sender.permission > MemberPermission.MEMBER) return true
        return false
    }
}