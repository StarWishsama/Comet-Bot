package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.bilibili.BiliBiliApi
import io.github.starwishsama.comet.api.bilibili.FakeClientApi
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.WrappedMessage
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.isNumeric
import io.github.starwishsama.comet.utils.toMirai
import kotlinx.coroutines.delay
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import org.apache.commons.lang3.Validate

class BiliBiliCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)) {
            if (args.isEmpty()) {
                return getHelp().toMirai()
            } else {
                when (args[0]) {
                    "sub", "订阅" -> {
                        try {
                            Validate.isTrue(args.size > 1, getHelp())
                            Validate.isTrue(user.isBotAdmin(), BotUtil.sendMsgPrefix("你没有权限"))

                            if (args[1].contains("|")) {
                                val users = args[1].split("|")
                                users.forEach {
                                    val result = subscribe(it)
                                    delay(500)
                                    return BotUtil.returnMsgIf(result is EmptyMessageChain, BotUtil.sendMsgPrefix("账号 $it 不存在").toMirai())
                                }
                                return BotUtil.sendMsgPrefix("订阅多个直播间成功 你好D啊").toMirai()
                            } else {
                                val result = subscribe(args[1])
                                return BotUtil.returnMsgIfElse(result is EmptyMessageChain, BotUtil.sendMsgPrefix("账号不存在").toMirai(), result)
                            }
                        } catch (e: IllegalArgumentException) {
                            val msg = e.message
                            if (msg != null) {
                                return msg.toMirai()
                            }
                        }
                    }
                    "unsub", "取消订阅" -> return unsubscribe(args)
                    "list" -> {
                        event.reply("请稍等...")
                        return getLiveStatus()
                    }
                    "info", "查询", "cx" -> {
                        return if (args.size > 1) {
                            event.quoteReply("请稍等...")
                            val item = FakeClientApi.getUser(args[1])
                            if (item != null) {
                                val before = item.title + "\n粉丝数: " + item.fans +
                                        "\n最近视频: " + (if (!item.avItems.isNullOrEmpty()) item.avItems[0].title else "没有投稿过视频") +
                                        "\n直播状态: " + (if (item.liveStatus == 1) "✔" else "✘") + "\n"
                                val dynamic = BiliBiliApi.getDynamic(item.mid)
                                before.toMirai() + getDynamicText(dynamic, event)
                            } else {
                                BotUtil.sendMsgPrefix("找不到对应的B站用户").toMirai()
                            }
                        } else getHelp().toMirai()
                    }
                    else -> return getHelp().toMirai()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
            CommandProps("bili", arrayListOf(), "订阅B站主播/查询用户动态", "nbot.commands.bili", UserLevel.USER)

    override fun getHelp(): String = """
        /bili sub [用户名] 订阅用户相关信息
        /bili unsub [用户名] 取消订阅用户相关信息
        /bili info [用户名] 查看用户的动态
    """.trimIndent()

    private suspend fun unsubscribe(args: List<String>): MessageChain {
        if (args.size > 1) {
            var roomId = 0L
            if (args[1].isNumeric()) {
                roomId = args[1].toLong()
            } else {
                val item = FakeClientApi.getUser(args[1])
                if (item != null) {
                    roomId = item.roomid
                }
            }

            return if (!BotVariables.cfg.subList.contains(roomId)) {
                BotUtil.sendMsgPrefix("你还没订阅直播间 ${args[1]}").toMirai()
            } else {
                BotVariables.cfg.subList.remove(args[1].toLong())
                BotUtil.sendMsgPrefix("取消订阅直播间 ${args[1]} 成功").toMirai()
            }
        } else {
            return getHelp().toMirai()
        }
    }

    private suspend fun getLiveStatus(): MessageChain {
        val subs = StringBuilder("监控室列表:\n")
        val info = ArrayList<com.hiczp.bilibili.api.live.model.RoomInfo>()

        for (l in BotVariables.cfg.subList) {
            val room = FakeClientApi.getLiveRoom(l)
            if (room != null) {
                info.add(room)
            }
        }

        info.sortByDescending { it.data.liveStatus == 1 }

        for (roomInfo in info) {
            subs.append(
                    "${BiliBiliApi.getUserNameByMid(roomInfo.data.uid)} " +
                            "${if (roomInfo.data.liveStatus == 1) "✔" else "✘"}\n"
            )
        }

        return subs.toString().trim().toMirai()
    }

    private suspend fun getDynamicText(dynamic: WrappedMessage?, event: MessageEvent): MessageChain {
        return if (dynamic == null) {
            ("\n无最近动态").toMirai()
        } else {
            if (dynamic.text != null) {
                dynamic.toMessageChain(event.subject)
            } else {
                ("\n无最近动态").toMirai()
            }
        }
    }

    private suspend fun subscribe(roomId: String): MessageChain {
        val rid: Long
        var name = ""
        rid = if (roomId.isNumeric()) {
            roomId.toLong()
        } else {
            val item = FakeClientApi.getUser(roomId)
            val title = item?.title
            if (title != null) name = title
            item?.roomid ?: return EmptyMessageChain
        }

        if (!BotVariables.cfg.subList.contains(rid)) {
            BotVariables.cfg.subList.add(rid)
        }

        return BotUtil.sendMsgPrefix("订阅 ${if (name.isNotBlank()) name else BiliBiliApi.getUserNameByMid(rid)}($rid) 成功")
            .toMirai()
    }

}