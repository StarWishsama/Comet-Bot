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
import io.github.starwishsama.comet.pushers.YoutubeStreamingChecker
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

@CometCommand
class YoutubeCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(user.id)) {
            if (args.isEmpty()) {
                return getHelp().convertToChain()
            } else {
                when (args[0]) {
                    "info" -> TODO("Youtube command haven't been implemented")
                    "sub" -> if (event is GroupMessageEvent) {
                        subscribeUser(args, event.group.id)
                    }
                    "unsub" -> if (event is GroupMessageEvent) {
                        unsubscribeUser(args, event.group.id)
                    }
                    "push" -> TODO("Youtube command haven't been implemented")
                    else -> getHelp().convertToChain()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps(
                name = "youtube",
                aliases = listOf("ytb", "y2b", "油管", "油土鳖"),
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

    private fun subscribeUser(args: List<String>, groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfigOrNew(groupId)
        if (args.size > 1) {
            if (!cfg.youtubeSubscribers.contains(args[1])) {
                val youtubeUserInfo: SearchVideoResult?

                try {
                    youtubeUserInfo = YoutubeApi.getChannelVideos(args[1])
                } catch (e: RateLimitException) {
                    return BotUtil.sendMessage(e.message)
                }

                if (youtubeUserInfo != null) {
                    cfg.youtubeSubscribers.add(args[1])
                    return BotUtil.sendMessage("订阅 ${youtubeUserInfo.items[0].snippet.channelTitle} 成功")
                }

                return BotUtil.sendMessage("订阅 ${args[1]} 失败")
            } else {
                return BotUtil.sendMessage("已经订阅过频道ID为 ${args[1]} 的频道了")
            }
        } else {
            return getHelp().convertToChain()
        }
    }

    private fun unsubscribeUser(args: List<String>, groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfigOrNew(groupId)
        return if (args.size > 1) {
            if (args[1] == "all" || args[1] == "全部") {

                cfg.youtubeSubscribers.forEach {
                    clearUnsubscribeUsersInPool(groupId, it)
                }

                cfg.youtubeSubscribers.clear()
                BotUtil.sendMessage("退订全部用户成功")
            } else if (cfg.youtubeSubscribers.contains(args[1])) {
                cfg.youtubeSubscribers.remove(args[1])
                clearUnsubscribeUsersInPool(groupId, args[1])
                BotUtil.sendMessage("退订 @${args[1]} 成功")
            } else {
                BotUtil.sendMessage("没有订阅过 @${args[1]}")
            }
        } else {
            getHelp().convertToChain()
        }
    }

    private fun clearUnsubscribeUsersInPool(groupId: Long, userName: String) {
        YoutubeStreamingChecker.pushPool.forEach { (username, cache) ->
            if (username == userName && cache.groups.contains(groupId)) {
                cache.groups.remove(groupId)
            }
        }
    }
}