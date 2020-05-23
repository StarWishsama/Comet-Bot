package io.github.starwishsama.nbot.commands.subcommands

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
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.*
import java.lang.Exception

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
                                event.quoteReply("正在查询...")
                                try {
                                    val twitterUser = TwitterApi.getUserInfo(args.getRestString(1))
                                    if (twitterUser == null) {
                                        BotUtil.sendMsgPrefix("找不到此用户或连接超时").toMirai()
                                    } else {
                                        try {
                                            val tweet = TwitterApi.getLatestTweet(args.getRestString(1))
                                            if (tweet != null) {
                                                val image = tweet.getPictureOrNull(event.subject)
                                                var result = (BotUtil.sendMsgPrefix(
                                                    "\n${twitterUser.name}\n" +
                                                            "粉丝数: ${twitterUser.followersCount}\n" +
                                                            "最近推文: \n${tweet.text}"
                                                ).toMirai())

                                                if (image != null) {
                                                    result += image
                                                }

                                                result
                                            } else {
                                                BotUtil.sendMsgPrefix("获取推文时出现了问题, 请查看后台").toMirai()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            BotUtil.sendMsgPrefix("获取推文时出现了问题, 请查看后台").toMirai()
                                        }
                                    }
                                } catch (e: RateLimitException) {
                                    BotUtil.sendMsgPrefix("API 调用已达上限").toMirai()
                                }
                            } else {
                                getHelp().toMirai()
                            }
                        }
                        "sub" -> {
                            "WIP".toMirai()
                        }
                        "unsub" -> {
                            "WIP".toMirai()
                        }
                        else -> getHelp().toMirai()
                    }
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("twitter", arrayListOf("twit", "蓝鸟"), "查询/订阅蓝鸟账号", "nbot.commands.twitter", UserLevel.USER)

    override fun getHelp(): String = """
        /twi info [蓝鸟ID] 查询账号信息
        /twi sub [蓝鸟ID] 订阅用户的推文
        /twi unsub [蓝鸟ID] 取消订阅用户的推文
    """.trimIndent()
}