package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.pojo.youtube.SearchVideoResult
import io.github.starwishsama.comet.objects.push.YoutubeUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import io.github.starwishsama.comet.utils.NumberUtil.getBetterNumber
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.util.*

@CometCommand
class YoutubeCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(user.id)) {
            if (args.isEmpty()) {
                return getHelp().convertToChain()
            } else {
                when (args[0]) {
                    "info" ->
                        return checkInfo(args[1]).toMessageChain(event.subject, true)
                    "sub" -> return if (event is GroupMessageEvent) {
                        subscribeUser(args[1], event.group.id)
                    } else {
                        "该功能仅限群聊使用".sendMessage()
                    }
                    "unsub" -> return if (event is GroupMessageEvent) {
                        unsubscribeUser(args, event.group.id)
                    } else {
                        "该功能仅限群聊使用".sendMessage()
                    }
                    "push" -> return if (event is GroupMessageEvent) {
                        val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
                        cfg.youtubePushEnabled = !cfg.youtubePushEnabled

                        "Youtube 推送状态: ${cfg.youtubePushEnabled}".sendMessage()
                    } else {
                        "该功能仅限群聊使用".sendMessage()
                    }
                    "list" -> return if (event is GroupMessageEvent) {
                        val cfg = GroupConfigManager.getConfigOrNew(event.group.id)
                        buildString {
                            append("已订阅账号: ")
                            cfg.youtubeSubscribers.forEach {
                                append(it.userName + "(" + it.id + ")").append(",")
                            }
                        }.removeSuffix(",").sendMessage()
                    } else {
                        "该功能仅限群聊使用".sendMessage()
                    }
                    else -> getHelp().convertToChain()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps(
                name = "youtube",
                aliases = listOf("ytb", "y2b", "油管"),
                description = "查询 Youtube 频道信息",
                permission = "nbot.commands.youtube",
                level = UserLevel.ADMIN
        )

    override fun getHelp(): String = """
        /youtube info [频道ID] 查询某个频道的信息
        /youtube sub [频道ID] 订阅某个频道的开播消息
        /youtube unsub [频道ID] 退订某个频道的开播消息
        /youtube push 开关本群推送功能
        
        该命令亦可用 /ytb|y2b|油管 等使用
        频道 ID 即为频道链接 channel 后面的一串字
    """.trimIndent()

    private fun subscribeUser(channelID: String, groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfigOrNew(groupId)

        if (!hasSubscribed(cfg.youtubeSubscribers, channelID).isPresent) {
            val youtubeUserInfo: SearchVideoResult?

            try {
                youtubeUserInfo = YoutubeApi.getChannelVideos(channelID)
            } catch (e: RateLimitException) {
                return sendMessage(e.message)
            }

            if (youtubeUserInfo != null) {
                cfg.youtubeSubscribers.add(YoutubeUser(channelID, youtubeUserInfo.items[0].snippet.channelTitle))
                return sendMessage("订阅 ${youtubeUserInfo.items[0].snippet.channelTitle} 成功")
            }

            return sendMessage("订阅 $channelID 失败")
        } else {
            return sendMessage("已经订阅过频道ID为 $channelID 的频道了")
        }
    }

    private fun unsubscribeUser(args: List<String>, groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfigOrNew(groupId)
        return if (args.size > 1) {
            if (args[1] == "all" || args[1] == "全部") {
                cfg.youtubeSubscribers.forEach {
                    clearUnsubscribeUsersInPool(groupId, it.id)
                }
                cfg.youtubeSubscribers.clear()
                sendMessage("退订全部用户成功")
            } else if (hasSubscribed(cfg.youtubeSubscribers, args[1]).isPresent) {
                val sub = hasSubscribed(cfg.youtubeSubscribers, args[1]).get()
                cfg.youtubeSubscribers.remove(sub)
                clearUnsubscribeUsersInPool(groupId, args[1])
                sendMessage("退订 ${sub.userName} 成功")
            } else {
                sendMessage("没有订阅过 @${args[1]}")
            }
        } else {
            getHelp().convertToChain()
        }
    }

    private fun clearUnsubscribeUsersInPool(groupId: Long, userName: String) {
        return
        /**YoutubeStreamingChecker.pushPool.forEach { (username, cache) ->
            if (username == userName && cache.groups.contains(groupId)) {
                cache.groups.remove(groupId)
            }
        }*/
    }

    private fun checkInfo(channelID: String): MessageWrapper {
        val result = YoutubeApi.getChannelByID(channelID) ?: return MessageWrapper("找不到该频道, 可能是 API 调用已达到上限?")
        val item = result.items[0]
        val text = """
        ${item.snippet.title}
> ${item.statistics.subscriberCount.getBetterNumber()}位订阅者 | ${item.statistics.viewCount.getBetterNumber()}次观看
> ${item.snippet.description.limitStringSize(50)}
        """.trimIndent()
        val wrapper = MessageWrapper(text)

        wrapper.plusImageUrl(item.snippet.thumbnails.asJsonObject["default"].asJsonObject["url"].asString)
        return wrapper
    }

    private fun hasSubscribed(subscribers: MutableSet<YoutubeUser>, channelID: String): Optional<YoutubeUser> {
        return subscribers.parallelStream().filter { it.id == channelID }.findAny()
    }
}