package io.github.starwishsama.comet.commands.subcommands.chats

import cn.hutool.http.HttpException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.NetUtil
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadAsImage
import kotlin.time.ExperimentalTime

class TwitterCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.id)) {
            if (BotVariables.cfg.twitterAccessToken == null) {
                return BotUtil.sendMessage("请到配置文件中填写推特 Token")
            }

            return if (args.isEmpty()) {
                getHelp().toMsgChain()
            } else {
                val id = if (event is GroupMessageEvent) event.group.id else args[2].toLong()

                when (args[0]) {
                    "info", "cx", "推文", "tweet", "查推" -> getTweetToMessageChain(args, event)
                    "sub", "订阅" -> subscribeUser(args, id)
                    "unsub", "退订" -> unsubscribeUser(args, id)
                    "list" -> {
                        val list = GroupConfigManager.getConfigSafely(id).twitterSubscribers
                        if (list.isEmpty()) "没有订阅任何蓝鸟用户".toMsgChain() else list.toString().toMsgChain()
                    }
                    "push" -> {
                        val switch = GroupConfigManager.getConfigSafely(id).twitterPushEnabled
                        GroupConfigManager.getConfigSafely(id).twitterPushEnabled = !switch
                        return BotUtil.sendMessage("蓝鸟动态推送已${if (switch) "开启" else "关闭"}")
                    }
                    else -> getHelp().toMsgChain()
                }
            }
        }
        return EmptyMessageChain
    }

    @ExperimentalTime
    private suspend fun getTweetToMessageChain(args: List<String>, event: MessageEvent): MessageChain {
        return if (args.size > 1) {
            event.quoteReply(BotUtil.sendMsgPrefix("正在查询, 请稍等"))
            try {
                if (args.size > 2) getTweetWithDesc(args[1], event.subject, args[2].toInt(), 1 + args[2].toInt())
                else getTweetWithDesc(args[1], event.subject, 0, 1)
            } catch (e: NumberFormatException) {
                BotUtil.sendMessage("请输入有效数字")
            }
        } else {
            getHelp().toMsgChain()
        }
    }

    @ExperimentalTime
    private suspend fun getTweetWithDesc(name: String, subject: Contact, index: Int = 1, max: Int = 10): MessageChain {
        val response = TwitterApi.getCachedTweet(name, index, max)
        val tweet: Tweet? = response.tweet
        val status: BotUtil.TaskStatus = response.status

        return if (tweet != null && status == BotUtil.TaskStatus.SUCCESS) {
            val resultMessage = BotUtil.sendMessage("\n${tweet.user.name}\n${tweet.getFullText()}")
            val imageUrl = tweet.getPictureUrl()
            var image: Image? = null
            if (imageUrl != null) image = NetUtil.getUrlInputStream(imageUrl)?.uploadAsImage(subject)

            if (image != null) resultMessage + image else resultMessage
        } else {
            when (status) {
                BotUtil.TaskStatus.FAILED -> BotUtil.sendMessage("获取推文时出现了异常, 请联系管理员")
                BotUtil.TaskStatus.TIMEOUT -> BotUtil.sendMessage("获取推文超时, 请稍后重试")
                BotUtil.TaskStatus.CUSTOMERROR -> BotUtil.sendMessage("已达到蓝鸟 API 请求上限啦, 请稍等一会再获取吧")
                else -> BotUtil.sendMessage("获取推文时出现了异常")
            }
        }
    }

    private fun subscribeUser(args: List<String>, groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfigSafely(groupId)
        if (args.size > 1) {
            if (!cfg.twitterSubscribers.contains(args[1])) {
                val twitter: TwitterUser?

                try {
                    twitter = TwitterApi.getUserInfo(args[1])
                } catch (e: HttpException) {
                    return BotUtil.sendMsgPrefix("连接至蓝鸟服务器超时, 等下再试试吧").toMsgChain()
                }

                if (twitter != null) {
                    cfg.twitterSubscribers.add(args[1])
                    return BotUtil.sendMsgPrefix("订阅 @${args[1]} 成功").toMsgChain()
                }

                return BotUtil.sendMsgPrefix("订阅 @${args[1]} 失败").toMsgChain()
            } else {
                return BotUtil.sendMsgPrefix("已经订阅过 @${args[1]} 了").toMsgChain()
            }
        } else {
            return getHelp().toMsgChain()
        }
    }

    private fun unsubscribeUser(args: List<String>, groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfigSafely(groupId)
        return if (args.size > 1) {
            if (cfg.twitterSubscribers.contains(args[1])) {
                cfg.twitterSubscribers.remove(args[1])
                BotUtil.sendMsgPrefix("退订 @${args[1]} 成功").toMsgChain()
            } else {
                BotUtil.sendMsgPrefix("没有订阅过 @${args[1]}").toMsgChain()
            }
        } else {
            getHelp().toMsgChain()
        }
    }

    override fun getProps(): CommandProps = CommandProps("twitter", arrayListOf("twit", "蓝鸟"), "查询/订阅蓝鸟账号", "nbot.commands.twitter", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /twit info [蓝鸟ID] 查询账号信息
        /twit sub [蓝鸟ID] 订阅用户的推文
        /twit unsub [蓝鸟ID] 取消订阅用户的推文
        /twit push 开启/关闭本群推文推送
    """.trimIndent()
}