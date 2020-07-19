package io.github.starwishsama.comet.commands.subcommands.chats

import cn.hutool.http.HttpException
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.EmptyTweetException
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.exceptions.TwitterApiException
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.BotUtil.getRestString
import io.github.starwishsama.comet.utils.toMirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

class TwitterCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)) {
            when {
                args.isEmpty() -> return getHelp().toMirai()
                BotVariables.cfg.twitterToken == null -> return BotUtil.sendMsgPrefix("请到配置文件中填写蓝鸟 Token").toMirai()
                else -> {
                    when (args[0]) {
                        "info", "cx", "查询" -> return searchUser(args, event)
                        "sub", "订阅" -> return subscribeUser(args)
                        "unsub", "退订" -> return unsubscribeUser(args)
                        "list" -> return BotVariables.cfg.twitterSubs.toString().toMirai()
                        else -> getHelp().toMirai()
                    }
                }
            }
        }
        return EmptyMessageChain
    }

    private suspend fun searchUser(args: List<String>, event: MessageEvent): MessageChain {
        if (args.size > 1) {
            event.quoteReply(BotUtil.sendMsgPrefix("正在查询, 请稍等"))
            try {
                return getTweetAsChain(args.getRestString(1), event.subject)
            } catch (e: RateLimitException) {
                return BotUtil.sendMsgPrefix("API 调用已达上限").toMirai()
            } catch (e: EmptyTweetException) {
                return BotUtil.sendMsgPrefixOrEmpty(e.message).toMirai()
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
        return EmptyMessageChain
    }

    private suspend fun getTweetAsChain(name: String, subject: Contact): MessageChain {
        val tweet: Tweet?
        try {
            tweet = TwitterApi.getTweetWithCache(name)
        } catch (x: RuntimeException) {
            return handleTimeout(x)
        }

        return if (tweet != null) {
            BotUtil.sendMsgPrefix("\n${tweet.user.name}\n").toMirai() + tweet.getAsMessageChain(subject)
        } else {
            BotUtil.sendMsgPrefix("获取推文时出现了意外").toMirai()
        }
    }

    private fun subscribeUser(args: List<String>): MessageChain {
        if (args.size > 1) {
            if (!BotVariables.cfg.twitterSubs.contains(args[1])) {
                val twitter: TwitterUser?

                try {
                    twitter = TwitterApi.getUserInfo(args[1])
                } catch (e: HttpException) {
                    return BotUtil.sendMsgPrefix("连接至蓝鸟服务器超时, 等下再试试吧").toMirai()
                }

                if (twitter != null) {
                    BotVariables.cfg.twitterSubs += args[1]
                    return BotUtil.sendMsgPrefix("订阅 @${args[1]} 成功").toMirai()
                }

                return BotUtil.sendMsgPrefix("订阅 @${args[1]} 失败").toMirai()
            } else {
                return BotUtil.sendMsgPrefix("已经订阅过 @${args[1]} 了").toMirai()
            }
        } else {
            return getHelp().toMirai()
        }
    }

    private fun unsubscribeUser(args: List<String>): MessageChain {
        return if (args.size > 1) {
            if (BotVariables.cfg.twitterSubs.contains(args[1])) {
                BotVariables.cfg.twitterSubs -= args[1]
                BotUtil.sendMsgPrefix("退订 @${args[1]} 成功").toMirai()
            } else {
                BotUtil.sendMsgPrefix("没有订阅过 @${args[1]}").toMirai()
            }
        } else {
            getHelp().toMirai()
        }
    }

    private fun handleTimeout(t: Throwable): MessageChain {
        return when (t) {
            is HttpException -> {
                when (t.cause) {
                    is ConnectException -> {
                        if (BotVariables.cfg.proxyPort != 0) {
                            BotUtil.sendMsgPrefix("无法连接到蓝鸟服务器").toMirai()
                        } else {
                            BotUtil.sendMsgPrefix("无法连接至代理服务器").toMirai()
                        }
                    }
                    is SocketTimeoutException -> BotUtil.sendMsgPrefix("连接超时").toMirai()
                    is SSLException -> BotUtil.sendMsgPrefix("连接超时").toMirai()
                    else -> {
                        Comet.logger.error(t)
                        BotUtil.sendMsgPrefix("获取推文时出现了意外").toMirai()
                    }
                }
            }
            is RateLimitException -> BotUtil.sendMsgPrefix("已达到蓝鸟 API 调用上限, 请等会再试吧").toMirai()
            else -> BotUtil.sendMsgPrefix("获取推文时出现了意外").toMirai()
        }
    }

    override fun getProps(): CommandProps = CommandProps("twitter", arrayListOf("twit", "蓝鸟"), "查询/订阅蓝鸟账号", "nbot.commands.twitter", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /twit info [蓝鸟ID] 查询账号信息
        /twit sub [蓝鸟ID] 订阅用户的推文
        /twit unsub [蓝鸟ID] 取消订阅用户的推文
    """.trimIndent()
}