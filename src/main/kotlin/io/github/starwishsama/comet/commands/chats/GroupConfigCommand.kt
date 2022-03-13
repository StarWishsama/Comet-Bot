/*
 * Copyright (c) 2019-2022 StarWishsama.
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
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.objects.wrapper.EmptyMessageWrapper
import io.github.starwishsama.comet.objects.wrapper.toMessageWrapper
import io.github.starwishsama.comet.service.command.GroupConfigService
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.sessions.toSessionTarget
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText

object GroupConfigCommand : ChatCommand, UnDisableableCommand, ConversationCommand {
    private val pendingConfigUser = mutableMapOf<SessionTarget, Long>()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (!hasPermission(user, event)) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (event !is GroupMessageEvent) {
            val session = Session(event.subject.toSessionTarget(), this)
            SessionHandler.insertSession(session)
            return "请输入欲修改设置群的群号, 输入 取消 取消操作".toMessageChain()
        }

        return if (args.isNotEmpty()) {
            val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
            handleCommand(args, cfg, event, event.bot)
        } else {
            getHelp().toMessageChain()
        }
    }

    override val props: CommandProps =
        CommandProps("group", arrayListOf("群设置", "gs"), "设置群内设置", UserLevel.USER)

    override fun getHelp(): String = """
        /group helper [@/QQ] 添加/删除群助理
        /group repeat 开启/关闭本群机器人复读功能
        /group autojoin 开启/关闭本群机器人自动接受加群请求
        /group func 启用/禁用本群可使用的命令
        /group newcomer 设置入群欢迎内容
        /group frm 设置群文件自动删除
    """.trimIndent()

    private fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
        return when {
            e is GroupMessageEvent -> e.sender.isOperator()
            user.hasPermission(props.permissionNodeName) -> true
            else -> false
        }
    }

    private fun handleCommand(args: List<String>, cfg: PerGroupConfig, event: MessageEvent, bot: Bot): MessageChain {
        when (args[0].lowercase()) {
            "helper", "群管" -> {
                return if (args.size == 2) {
                    val target = CometUtil.parseAtAsBotUser(event.message, args[1])
                    if (target != null) {
                        GroupConfigService.setHelper(cfg, target.id).toMessageChain(event.subject)
                    } else {
                        toMessageChain("找不到你想要添加/删除的用户")
                    }
                } else {
                    toMessageChain(getHelp())
                }
            }
            "repeat" -> {
                return toMessageChain("已${if (GroupConfigService.setRepeat(cfg)) "开启" else "关闭"}群复读机")
            }
            "autojoin" -> {
                if (args.size > 1) {
                    return when (args[1]) {
                        "condition", "条件", "tj", "cd" -> {
                            if (args.size > 2) {
                                GroupConfigService.setAutoAcceptRequest(cfg, true, args.getRestString(2)).toMessageChain(event.subject)
                            } else {
                                "/gs autojoin condition [关键词]\n满足关键词的入群申请会自动通过".toMessageChain()
                            }
                        }
                        else -> {
                            "/gs autojoin condition [关键词]\n满足关键词的入群申请会自动通过".toMessageChain()
                        }
                    }
                } else {
                    return if (bot.getGroup(cfg.id)?.botPermission == MemberPermission.MEMBER) {
                        "抱歉, 机器人不是群管, 无法自动接受加群请求.".toMessageChain()
                    } else {
                        GroupConfigService.setAutoAcceptRequest(cfg, !cfg.autoAccept).toMessageChain(event.subject)
                    }
                }
            }
            "function", "fun", "func" -> {
                if (args.size < 2) {
                    return toMessageChain(
                        """
                现在支持禁用彗星 Bot 的命令功能了!
                /gs function [命令名] 在本群禁用指定命令
            """.trimIndent()
                    )
                }

                return GroupConfigService.disableCommand(cfg, args[1]).toMessageChain(event.subject)
            }
            "newcomer", "入群欢迎", "自动欢迎", "zdhy", "rqhy" -> {
                if (args.size > 1) {
                    return if (args.size > 2) {
                        val welcomeText = MessageChainBuilder().append(args.getRestString(2)).apply {
                            event.message.filter { it !is PlainText }.forEach { element ->
                                append(element)
                            }
                        }.build().toMessageWrapper()

                        return GroupConfigService.setNewComerMessage(cfg, welcomeText).toMessageChain(event.subject)
                    } else {
                        "/group newcomer [入群欢迎内容]\n内容支持纯文字 + 图片, 想要 @ 入群成员请用 [At] 代替.".toMessageChain()
                    }
                } else {
                    return GroupConfigService.setNewComerMessage(cfg, EmptyMessageWrapper).toMessageChain(event.subject)
                }
            }
            "fileremove", "filerm", "frm", "群文件自动删除", "文件删除" -> {
                return if (args.size > 1) {
                    when (args[1]) {
                        "switch" -> {
                            GroupConfigService.setFileRemove(cfg).toMessageChain(event.subject)
                        }
                        "delay" -> {
                            if (args.size > 2) {
                                val delay = args[2].toLongOrNull() ?: return "请输入正确的数字".toMessageChain()
                                GroupConfigService.setFileRemoveDelay(cfg, delay).toMessageChain(event.subject)
                            } else {
                                "/group frm delay [删除延迟]".toMessageChain()
                            }
                        }
                        "pattern" -> {
                            return if (args.size > 2) {
                                GroupConfigService.setFileRemovePattern(cfg, args.getRestString(2)).toMessageChain(event.subject)
                            } else {
                                "/group frm pattern [文件匹配正则表达式]".toMessageChain()
                            }
                        }
                        else -> {
                            """
                            /group frm switch 开启或关闭自动删除文件
                            /group frm pattern [文件匹配正则表达式] 设置文件匹配正则表达式
                            /group frm delay [删除延迟] 设置文件删除延迟
                            """.trimIndent().toMessageChain()
                        }
                    }
                } else {
                    getHelp().toMessageChain()
                }
            }
            else -> {
                return getHelp().toMessageChain()
            }
        }
    }

    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        if (session.isExpired(30) || event.message.contentToString() == "退出") {
            SessionHandler.removeSession(session)
            return
        }

        val args = event.message.contentToString().split(" ")

        if (!pendingConfigUser.contains(session.target)) {
            val cfg = GroupConfigManager.getConfig(session.target.groupId)

            if (cfg == null) {
                event.subject.sendMessage("找不到对应群的配置信息".toMessageChain())
            } else {
                pendingConfigUser[session.target] = session.target.groupId

                event.subject.sendMessage(handleCommand(args, cfg, event, event.bot))
            }
        }
    }
}