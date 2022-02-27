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
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.UserApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.feed.toMessageWrapper
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.BiliBiliService
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.TaskUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.lang.Thread.sleep

object BiliBiliCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (!hasPermission(user, event)) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (args.isEmpty()) {
            return getHelp().convertToChain()
        }

        when (args[0]) {
            "sub", "订阅" -> return BiliBiliService.callSubscribeUser(user, args, event)
            "unsub", "取消订阅" -> {
                return if (event is GroupMessageEvent) {
                    BiliBiliService.callUnsubscribeUser(args, event.group.id)
                } else {
                    toMessageChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            "list" -> {
                event.subject.sendMessage("请稍等...")
                return BiliBiliService.getSubscribers(event)
            }
            "dynamic", "动态", "dt" -> {
                return if (args.size > 1) {
                    BiliBiliService.searchUserDynamic(args.getRestString(1), event)
                    EmptyMessageChain
                } else {
                    getHelp().convertToChain()
                }
            }
            "user", "用户", "yh" -> {
                return if (args.size > 1) {
                    BiliBiliService.searchUserInfo(args.getRestString(1), event)
                    EmptyMessageChain
                } else getHelp().convertToChain()
            }
            "video", "vd", "视频" -> {
                return if (args.size > 1) {
                    BiliBiliService.searchVideo(args.getRestString(1), event)
                } else {
                    getHelp().toMessageChain()
                }
            }
            "push" -> {
                return if (event is GroupMessageEvent) {
                    if (user.isBotAdmin() || event.sender.isOperator()) {
                        val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
                        cfg.biliPushEnabled = !cfg.biliPushEnabled
                        "B站动态推送功能已${if (cfg.biliPushEnabled) "开启" else "关闭"}".toMessageChain()
                    } else {
                        LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
                    }
                } else {
                    toMessageChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            "refresh" -> {
                if (event is GroupMessageEvent) {
                    val cfg = GroupConfigManager.getConfig(event.group.id) ?: return "本群尚未注册至 Comet".toMessageChain()

                    TaskUtil.schedule {
                        cfg.biliSubscribers.forEach {
                            it.userName = UserApi.getUserNameByMid(it.id.toInt())
                            runBlocking { it.roomID = UserApi.getUserSpace(it.id.toInt())?.liveRoom?.roomId ?: -1 }
                            sleep(1_500)
                        }
                    }

                    return "正在刷新动态, 请稍候".toMessageChain()
                } else {
                    toMessageChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            "id" -> {
                if (args[1].isNumeric()) {
                    val dynamic = DynamicApi.getDynamicById(args[1].toLong())
                    return dynamic?.toMessageWrapper()?.toMessageChain(event.subject) ?: "找不到对应的动态".toMessageChain()
                } else {
                    "请输入有效的动态 ID".toMessageChain()
                }
            }
            "parse" -> {
                return if (event is GroupMessageEvent) {
                    BiliBiliService.setParseVideo(event.group.id)
                } else {
                    toMessageChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            else -> return getHelp().convertToChain()
        }

        return EmptyMessageChain
    }

    override val props: CommandProps =
        CommandProps("bili", arrayListOf(), "订阅查询B站主播/用户动态", UserLevel.USER)

    override fun getHelp(): String = """
        /bili sub [用户名/UID] 订阅用户相关信息
        /bili unsub [用户名/UID] 取消订阅用户相关信息
        /bili user [用户名/UID] 查看指定用户信息
        /bili dynamic [用户名/UID] 查看指定用户动态
        /bili push 开启/关闭本群动态/开播推送
        /bili refresh 刷新订阅UP主缓存
        /bili id [动态 ID] 通过动态 ID 查询动态
        /bili parse 开启/关闭群聊消息视频解析
    """.trimIndent()

    fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
        if (user.hasPermission(props.permissionNodeName)) return true
        if (e is GroupMessageEvent && e.sender.permission >= MemberPermission.MEMBER) return true
        return false
    }

}