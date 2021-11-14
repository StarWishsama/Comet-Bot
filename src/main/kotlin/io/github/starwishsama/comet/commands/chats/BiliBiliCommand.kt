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

import io.github.starwishsama.comet.CometVariables.localizationManager
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.UserApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToWrapper
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.BiliBiliService
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.TaskUtil
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.lang.Thread.sleep

class BiliBiliCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (!hasPermission(user, event)) {
            return localizationManager.getLocalizationText("message.no-permission").toChain()
        }

        if (args.isEmpty()) {
            return getHelp().convertToChain()
        }

        when (args[0]) {
            "sub", "订阅" -> return BiliBiliService.callSubscribeUser(this, user, args, event)
            "unsub", "取消订阅" -> {
                return if (event is GroupMessageEvent) {
                    BiliBiliService.callUnsubscribeUser(this, args, event.group.id)
                } else {
                    toChain("抱歉, 该命令仅限群聊使用!")
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
            "info", "查询", "cx" -> {
                return if (args.size > 1) {
                    BiliBiliService.searchUserInfo(args.getRestString(1), event)
                    EmptyMessageChain
                } else getHelp().convertToChain()
            }
            "push" -> {
                return if (event is GroupMessageEvent) {
                    if (user.isBotAdmin() || event.sender.isOperator()) {
                        val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
                        cfg.biliPushEnabled = !cfg.biliPushEnabled
                        "B站动态推送功能已${if (cfg.biliPushEnabled) "开启" else "关闭"}".toChain()
                    } else {
                        localizationManager.getLocalizationText("message.no-permission").toChain()
                    }
                } else {
                    toChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            "refresh" -> {
                if (event is GroupMessageEvent) {
                    val cfg = GroupConfigManager.getConfig(event.group.id) ?: return "本群尚未注册至 Comet".toChain()

                    TaskUtil.schedule {
                        cfg.biliSubscribers.forEach {
                            it.userName = DynamicApi.getUserNameByMid(it.id.toLong())
                            it.roomID = UserApi.userApiService.getUserInfo(it.id.toLong()).execute()
                                .body()?.data?.liveRoomInfo?.roomID ?: -1
                            sleep(1_500)
                        }
                    }

                    return "正在刷新动态, 请稍候".toChain()
                } else {
                    toChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            "id" -> {
                if (args[1].isNumeric()) {
                    val dynamic = DynamicApi.getDynamicById(args[1].toLong())
                    return dynamic.convertToWrapper().toMessageChain(event.subject)
                } else {
                    "请输入有效的动态 ID".toChain()
                }
            }
            "parse" -> {
                return if (event is GroupMessageEvent) {
                    BiliBiliService.setParseVideo(event.group.id)
                } else {
                    toChain("抱歉, 该命令仅限群聊使用!")
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
        /bili info [用户名/UID] 查看指定用户信息
        /bili dynamic [用户名/UID] 查看指定用户动态
        /bili push 开启/关闭本群开播推送
        /bili refresh 刷新订阅UP主缓存
        /bili id [动态 ID] 通过动态 ID 查询动态
        /bili parse 开启/关闭群聊消息视频解析
    """.trimIndent()

    fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
        val level = props.level
        if (user.compareLevel(level)) return true
        if (e is GroupMessageEvent && e.sender.permission >= MemberPermission.MEMBER) return true
        return false
    }

}