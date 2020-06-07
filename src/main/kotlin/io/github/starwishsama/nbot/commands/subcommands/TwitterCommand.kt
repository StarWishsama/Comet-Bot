package io.github.starwishsama.nbot.commands.subcommands

import cn.hutool.http.HttpException
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.api.twitter.TwitterApi
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.exceptions.EmptyTweetException
import io.github.starwishsama.nbot.exceptions.RateLimitException
import io.github.starwishsama.nbot.exceptions.TwitterApiException
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.pojo.twitter.Tweet
import io.github.starwishsama.nbot.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.getRestString
import io.github.starwishsama.nbot.util.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.net.ConnectException
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
                    when (args[0]) {
                        "info", "cx", "查询" -> {
                            if (args.size > 1) {
                                event.quoteReply(BotUtil.sendMsgPrefix("正在查询, 请稍等"))

                                try {
                                    val tweet: Tweet?

                                    try {
                                        tweet = TwitterApi.getTweetWithCache(args.getRestString(1))
                                    } catch (x: RuntimeException) {
                                        return when (x) {
                                            is HttpException -> {
                                                when (x.cause) {
                                                    is ConnectException -> {
                                                        if (BotConstants.cfg.proxyPort != 0)
                                                            BotUtil.sendMsgPrefix("无法连接到蓝鸟服务器").toMirai()
                                                        else
                                                            BotUtil.sendMsgPrefix("无法连接至代理服务器").toMirai()
                                                    }
                                                    is SocketTimeoutException -> BotUtil.sendMsgPrefix("连接超时").toMirai()
                                                    else -> {
                                                        BotMain.logger.error(x)
                                                        BotUtil.sendMsgPrefix("获取推文时出现了意外").toMirai()
                                                    }
                                                }
                                            }
                                            is RateLimitException -> BotUtil.sendMsgPrefix("已达到蓝鸟 API 调用上限, 请等会再试吧")
                                                .toMirai()
                                            else -> BotUtil.sendMsgPrefix("获取推文时出现了意外").toMirai()
                                        }
                                    }

                                    if (tweet != null) {
                                        val image = tweet.getPictureOrNull(event.subject)
                                        var result =
                                            BotUtil.sendMsgPrefix("\n${tweet.user.name}\n${tweet.getFullText()}")
                                                .toMirai()

                                        if (image != null) {
                                            result += image
                                        }

                                        return result
                                    }

                                } catch (e: RateLimitException) {
                                    return BotUtil.sendMsgPrefix("API 调用已达上限").toMirai()
                                } catch (e: EmptyTweetException) {
                                    return BotUtil.sendMsgPrefix(e.message ?: "").toMirai()
                                } catch (e: TwitterApiException) {
                                    if (e.code == 34) {
                                        return BotUtil.sendMsgPrefix("找不到此蓝鸟用户").toMirai()
                                    } else {
                                        BotUtil.sendMsgPrefix("获取推文时出现了意外").toMirai()
                                    }
                                }
                            } else {
                                return getHelp().toMirai()
                            }
                        }
                        "sub", "订阅" -> {
                            if (args.size > 1) {
                                if (!BotConstants.cfg.twitterSubs.contains(args[1])) {
                                    var twitter: TwitterUser? = null

                                    try {
                                        twitter = TwitterApi.getUserInfo(args[1])
                                    } catch (e: HttpException) {
                                        when (e.cause) {
                                            is SocketTimeoutException -> return BotUtil.sendMsgPrefix("连接至蓝鸟服务器超时, 等下再试试吧")
                                                .toMirai()
                                        }
                                    }

                                    if (twitter != null) {
                                        BotConstants.cfg.twitterSubs += args[1]
                                        return BotUtil.sendMsgPrefix("订阅 @${args[1]} 成功").toMirai()
                                    }

                                    return BotUtil.sendMsgPrefix("订阅 @${args[1]} 失败").toMirai()
                                } else {
                                    return BotUtil.sendMsgPrefix("你已经订阅了 @${args[1]}").toMirai()
                                }
                            } else {
                                return getHelp().toMirai()
                            }
                        }
                        "unsub", "退订" -> {
                            return if (args.size > 1) {
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
                        "list" -> {
                            return BotConstants.cfg.twitterSubs.toString().toMirai()
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