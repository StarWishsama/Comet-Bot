package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables.localizationManager

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.FakeClientApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.UserApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.user.UserInfo
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.NumberUtil.getBetterNumber
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BiliBiliCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().convertToChain()
        }

        when (args[0]) {
            "sub", "订阅" -> return advancedSubscribe(user, args, event)
            "unsub", "取消订阅" -> {
                return if (event is GroupMessageEvent) {
                    unsubscribe(args, event.group.id)
                } else {
                    toChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            "list" -> {
                event.subject.sendMessage("请稍等...")
                return getSubList(event)
            }
            "info", "查询", "cx" -> {
                return if (args.size > 1) {
                    if (!FakeClientApi.client.isLogin) {
                        event.subject.sendMessage(event.message.quote() + "请稍等...")
                        val item = FakeClientApi.getUser(args[1])
                        if (item != null) {
                            val text = item.title + "\n粉丝数: " + item.fans.getBetterNumber() +
                                    "\n最近视频: " + (if (!item.avItems.isNullOrEmpty()) item.avItems[0].title else "没有投稿过视频") +
                                    "\n直播状态: " + (if (item.liveStatus == 1) "✔" else "✘") + "\n"
                            val dynamic = DynamicApi.getWrappedDynamicTimeline(item.mid)
                            text.convertToChain() + getDynamicText(dynamic, event)
                        } else {
                            "找不到对应的B站用户".toChain()
                        }
                    } else {
                        "未登录无法使用查询功能, 请在配置中配置B站账号密码".toChain()
                    }
                } else getHelp().convertToChain()
            }
            "push" -> {
                return if (event is GroupMessageEvent) {
                    if (user.isBotAdmin() || event.sender.isOperator()) {
                        val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
                        cfg.biliPushEnabled = !cfg.biliPushEnabled
                        "B站动态推送功能已${if (cfg.biliPushEnabled) "开启" else "关闭"}".toChain()
                    } else {
                        localizationManager.getLocalizationText("msg.no-permission").toChain()
                    }
                } else {
                    toChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            "refresh" -> {
                if (event is GroupMessageEvent) {
                    val cfg = GroupConfigManager.getConfig(event.group.id) ?: return "本群尚未注册至 Comet".toChain()
                    cfg.biliSubscribers.forEach {
                        it.userName = DynamicApi.getUserNameByMid(it.id.toLong())
                        it.roomID = UserApi.userApiService.getMemberInfoById(it.id.toLong()).execute()
                            .body()?.data?.liveRoomInfo?.roomId ?: -1
                        delay(1_500)
                    }
                    return "刷新缓存成功".toChain()
                } else {
                    toChain("抱歉, 该命令仅限群聊使用!")
                }
            }
            else -> return getHelp().convertToChain()
        }

        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("bili", arrayListOf(), "订阅查询B站主播/用户动态", "nbot.commands.bili", UserLevel.USER)

    override fun getHelp(): String = """
        /bili sub [用户名] 订阅用户相关信息
        /bili unsub [用户名] 取消订阅用户相关信息
        /bili info [用户名] 查看用户的动态
        /bili push 开启/关闭本群开播推送
        /bili refresh 刷新订阅UP主缓存
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        val level = getProps().level
        if (user.compareLevel(level)) return true
        if (e is GroupMessageEvent && e.sender.permission >= MemberPermission.MEMBER) return true
        return false
    }

    private suspend fun advancedSubscribe(user: BotUser, args: List<String>, event: MessageEvent): MessageChain {
        try {
            if (args.size <= 1) return getHelp().convertToChain()

            if (!hasPermission(user, event)) {
                return localizationManager.getLocalizationText("msg.no-permission").convertToChain()
            }

            return if (args[1].contains("|")) {
                val users = args[1].split("|")
                val id = if (event is GroupMessageEvent) event.group.id else args[2].toLong()
                subscribeUsers(users, id) ?: toChain("订阅多个用户成功", true)
            } else {
                val id = if (event is GroupMessageEvent) event.group.id else args[2].toLong()
                val result = subscribe(args[1], id)
                if (result is EmptyMessageChain) {
                    toChain("账号不存在")
                } else {
                    result
                }
            }
        } catch (e: IllegalArgumentException) {
            return toChain(e.message, true)
        }
    }

    private suspend fun subscribeUsers(users: List<String>, id: Long): MessageChain? {
        users.forEach {
            val result = subscribe(it, id)
            delay(500)
            if (result is EmptyMessageChain) {
                return toChain("账号 $it 不存在")
            }
        }
        return null
    }

    private fun unsubscribe(args: List<String>, groupId: Long): MessageChain {
        return if (args.size > 1) {
            val cfg = GroupConfigManager.getConfigOrNew(groupId)

            if (args[1] == "all" || args[1] == "全部") {
                cfg.biliSubscribers.clear()
                return toChain("退订全部成功")
            }

            val item = if (args[1].isNumeric()) {
                val item = cfg.biliSubscribers.parallelStream().filter { it.id == args[1] }.findFirst()
                if (!item.isPresent) {
                    return "找不到你要退订的用户".toChain()
                }

                item.get()
            } else {
                val item = cfg.biliSubscribers.parallelStream().filter { it.userName == args[1] }.findFirst()
                if (!item.isPresent) {
                    return "找不到你要退订的用户".toChain()
                }

                item.get()
            }

            cfg.biliSubscribers.remove(item)
            toChain("取消订阅用户 ${item.userName} 成功")
        } else {
            getHelp().convertToChain()
        }
    }

    private fun getSubList(event: MessageEvent): MessageChain {
        if (event !is GroupMessageEvent) return toChain("只能在群里查看订阅列表")
        val list = GroupConfigManager.getConfig(event.group.id)?.biliSubscribers

        if (list?.isNotEmpty() == true) {
            val subs = buildString {
                append("监控室列表:\n")
                list.forEach {
                    append(it.userName + " (${it.id}, ${it.roomID})\n")
                    trim()
                }
            }
            return toChain(subs)
        }
        return toChain("未订阅任何用户")
    }

    private fun getDynamicText(dynamic: MessageWrapper?, event: MessageEvent): MessageChain {
        return if (dynamic == null) {
            PlainText("\n没有发送过动态").toMessageChain()
        } else {
            if (dynamic.getAllText().isNotEmpty()) {
                dynamic.toMessageChain(event.subject)
            } else {
                PlainText("\n没有发送过动态").toMessageChain()
            }
        }
    }

    private suspend fun subscribe(target: String, groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfigOrNew(groupId)
        var name = ""
        val uid: Long = when {
            target.isNumeric() -> {
                name = DynamicApi.getUserNameByMid(target.toLong())
                target.toLong()
            }
            else -> {
                if (!FakeClientApi.client.isLogin) {
                    return "未登录无法使用昵称搜索订阅, 请在配置中配置B站账号密码".toChain()
                }

                val item = FakeClientApi.getUser(target)
                val title = item?.title
                if (title != null) name = title
                item?.mid ?: return EmptyMessageChain
            }
        }

        var roomNumber: Long = -1

        UserApi.userApiService.getMemberInfoById(uid).enqueue(
            object : Callback<UserInfo> {
                override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                    val info = response.body()
                    if (info != null) {
                        roomNumber = info.data.liveRoomInfo.roomId
                    }
                }

                override fun onFailure(call: Call<UserInfo>, t: Throwable) {}
            }
        )

        return if (!cfg.biliSubscribers.stream().filter { it.id.toLong() == uid }.findFirst().isPresent) {
            cfg.biliSubscribers.add(BiliBiliUser(uid.toString(), name, roomNumber))
            toChain("订阅 ${name}($uid) 成功")
        } else {
            toChain("你已经订阅过 ${name}($uid) 了!")
        }
    }

}