package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables.daemonLogger

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.api.thirdparty.twitter.data.TwitterUser
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.config.api.TwitterConfig
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain
import kotlin.time.ExperimentalTime


class TwitterCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (ApiManager.getConfig<TwitterConfig>().token.isEmpty()) {
            return CometUtil.toChain("推特推送未被正确设置, 请联系机器人管理员")
        }

        return if (args.isEmpty()) {
            getHelp().convertToChain()
        } else {
            val id: Long = when {
                event is GroupMessageEvent -> event.group.id
                args.size > 1 -> args[2].toLong()
                else -> -1
            }

            when (args[0]) {
                "info", "cx", "推文", "tweet", "查推" -> getTweetToMessageChain(args, event)
                "sub", "订阅" -> subscribeUser(args, id)
                "unsub", "退订" -> unsubscribeUser(args, id)
                "list" -> {
                    val list = GroupConfigManager.getConfigOrNew(id).twitterSubscribers
                    if (list.isEmpty()) "没有订阅任何推特用户".convertToChain() else list.toString().convertToChain()
                }
                "push" -> {
                    val cfg = GroupConfigManager.getConfigOrNew(id)
                    cfg.twitterPushEnabled = !cfg.twitterPushEnabled
                    return CometUtil.toChain("推特动态推送已${if (cfg.twitterPushEnabled) "开启" else "关闭"}")
                }
                "id" -> {
                    return if (args[1].isNumeric()) {
                        getTweetByID(args[1].toLong(), event.subject)
                    } else {
                        "请输入有效数字".convertToChain()
                    }
                }
                "nopic" -> {
                    val cfg = GroupConfigManager.getConfigOrNew(id)
                    cfg.twitterPictureMode = !cfg.twitterPictureMode
                    return CometUtil.toChain("推特动态推送图片已${if (cfg.twitterPushEnabled) "开启" else "关闭"}")
                }
                else -> getHelp().convertToChain()
            }
        }
    }

    override fun getProps(): CommandProps =
        CommandProps("data", arrayListOf("twit", "推特", "tt"), "查询/订阅推特账号", "nbot.commands.data", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /twit info [推特ID] 查询账号信息
        /twit sub [推特ID] 订阅用户的推文
        /twit unsub [推特ID] 取消订阅用户的推文
        /twit push 开启/关闭本群推文推送
        /twit id [推文ID] 通过推文ID查询推文
        /twit cx 查询订阅列表
        
        命令别名: /推特 /tt /twitter
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        if (super.hasPermission(user, e)) return true
        if (e is GroupMessageEvent && e.sender.permission != MemberPermission.MEMBER) return true
        return false
    }

    @ExperimentalTime
    private suspend fun getTweetToMessageChain(args: List<String>, event: MessageEvent): MessageChain {
        return if (args.size > 1) {
            event.subject.sendMessage(event.message.quote() + CometUtil.toChain("正在查询, 请稍等"))
            try {
                if (args.size > 2) getTweetWithDesc(args[1], event.subject, args[2].toInt(), 1 + args[2].toInt())
                else getTweetWithDesc(args[1], event.subject, 0, 1)
            } catch (e: NumberFormatException) {
                CometUtil.toChain("请输入有效数字")
            }
        } else {
            getHelp().convertToChain()
        }
    }

    @ExperimentalTime
    private fun getTweetWithDesc(name: String, subject: Contact, index: Int = 1, max: Int = 10): MessageChain {
        return try {
            val tweet = TwitterApi.getTweetInTimeline(name, index, max)
            if (tweet != null) {
                return CometUtil.toChain("\n${tweet.user.name}\n\n") + tweet.toMessageChain(subject)
            } else {
                CometUtil.toChain("获取到的推文为空")
            }
        } catch (t: Throwable) {
            if (NetUtil.isTimeout(t)) {
                CometUtil.toChain("获取推文时连接超时")
            } else {
                daemonLogger.warning(t)
                CometUtil.toChain("获取推文时出现了异常")
            }
        }
    }

    private fun subscribeUser(args: List<String>, groupId: Long): MessageChain {
        if (groupId > 0) {
            val cfg = GroupConfigManager.getConfigOrNew(groupId)
            if (args.size > 1) {
                if (!cfg.twitterSubscribers.contains(args[1])) {
                    val twitterUser: TwitterUser

                    try {
                        twitterUser = TwitterApi.getUserProfile(-1, args[1])[0]
                    } catch (e: RateLimitException) {
                        return CometUtil.toChain("订阅 @${args[1]} 失败")
                    }

                    cfg.twitterSubscribers.add(args[1])
                    return CometUtil.toChain("订阅 ${twitterUser.name}(@${twitterUser.twitterId}) 成功")
                } else {
                    return CometUtil.toChain("已经订阅过 @${args[1]} 了")
                }
            } else {
                return getHelp().convertToChain()
            }
        } else {
            return CometUtil.toChain("请填写正确的群号!")
        }
    }

    private fun unsubscribeUser(args: List<String>, groupId: Long): MessageChain {
        if (groupId > 0) {
            val cfg = GroupConfigManager.getConfigOrNew(groupId)
            return if (args.size > 1) {
                if (args[1] == "all" || args[1] == "全部") {
                    cfg.twitterSubscribers.clear()
                    CometUtil.toChain("退订全部用户成功")
                } else if (cfg.twitterSubscribers.contains(args[1])) {
                    cfg.twitterSubscribers.remove(args[1])
                    CometUtil.toChain("退订 @${args[1]} 成功")
                } else {
                    CometUtil.toChain("没有订阅过 @${args[1]}")
                }
            } else {
                getHelp().convertToChain()
            }
        } else {
            return CometUtil.toChain("请填写正确的群号!")
        }
    }

    @ExperimentalTime
    private fun getTweetByID(id: Long, target: Contact): MessageChain =
        TwitterApi.getTweetById(id)?.toMessageChain(target)
            ?: PlainText("找不到对应ID的推文").toMessageChain()
}