package io.github.starwishsama.nbot.commands.subcommands

import cn.hutool.http.HttpException
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.exceptions.RateLimitException
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.getRestString
import io.github.starwishsama.nbot.util.toMirai
import io.github.starwishsama.nbot.api.twitter.TwitterApi
import io.github.starwishsama.nbot.exceptions.EmptyTweetException
import io.github.starwishsama.nbot.objects.pojo.twitter.TwitterUser
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException

class TwitterCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)) {
            when {
                args.isEmpty() -> {
                    return getHelp().toMirai()
                }
                BotConstants.cfg.twitterToken == null -> {
                    return BotUtil.sendMsgPrefix("请到配置文件中填写蓝鸟 Token").toMirai()
                }
                else -> {
                    return when (args[0]) {
                        "info", "cx", "查询" -> {
                            if (args.size > 1) {
                                event.quoteReply(BotUtil.sendMsgPrefix("正在查询, 请稍等"))
                                try {
                                    val tweet = TwitterApi.getLatestTweet(args.getRestString(1))
                                    if (tweet == null) {
                                        BotUtil.sendMsgPrefix("找不到此用户|该用户没有发送过推文|连接超时").toMirai()
                                    } else {
                                        val image = tweet.getPictureOrNull(event.subject)
                                        var result = (BotUtil.sendMsgPrefix(
                                            "\n${tweet.user.name}\n" +
                                                    "粉丝数: ${tweet.user.followersCount}\n" +
                                                    "最近推文: \n${tweet.getFullText()}"
                                        ).toMirai())

                                        if (image != null) {
                                            result += image
                                        }

                                        result
                                    }
                                } catch (e: RateLimitException) {
                                    BotUtil.sendMsgPrefix("API 调用已达上限").toMirai()
                                } catch (e: EmptyTweetException) {
                                    BotUtil.sendMsgPrefix(e.message ?: "").toMirai()
                                }
                            } else {
                                getHelp().toMirai()
                            }
                        }
                        "sub" -> {
                            if (args.size > 1) {
                                if (!BotConstants.cfg.twitterSubs.contains(args[1])) {
                                    var twitter : TwitterUser? = null

                                    try {
                                        twitter = TwitterApi.getUserInfo(args[1])
                                    } catch (e: HttpException) {
                                        when (e.cause) {
                                            is SocketTimeoutException -> BotUtil.sendMsgPrefix("连接至蓝鸟服务器超时, 等下再试试吧").toMirai()
                                        }
                                    }

                                    if (twitter != null) {
                                        BotConstants.cfg.twitterSubs += args[1]
                                        BotUtil.sendMsgPrefix("订阅 @${args[1]} 成功").toMirai()
                                    }

                                    BotUtil.sendMsgPrefix("订阅 @${args[1]} 失败").toMirai()
                                } else {
                                    BotUtil.sendMsgPrefix("你已经订阅了 @${args[1]}").toMirai()
                                }
                            } else {
                                getHelp().toMirai()
                            }
                        }
                        "unsub" -> {
                            if (args.size > 1) {
                                if (BotConstants.cfg.twitterSubs.contains(args[1])) {
                                    BotConstants.cfg.twitterSubs -= args[1]
                                    BotUtil.sendMsgPrefix("退订 @${args[1]} 成功").toMirai()
                                } else {
                                    BotUtil.sendMsgPrefix("没有订阅过 @${args[1]}").toMirai()
                                }
                            } else {
                                getHelp().toMirai()
                            }
                        }
                        else -> getHelp().toMirai()
                    }
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("twitter", arrayListOf("twit", "蓝鸟"), "查询/订阅蓝鸟账号", "nbot.commands.twitter", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /twit info [蓝鸟ID] 查询账号信息
        /twit sub [蓝鸟ID] 订阅用户的推文
        /twit unsub [蓝鸟ID] 取消订阅用户的推文
    """.trimIndent()
}