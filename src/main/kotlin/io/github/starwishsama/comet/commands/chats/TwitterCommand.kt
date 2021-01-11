package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.pojo.twitter.TwitterUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.NetUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain
import kotlin.time.ExperimentalTime

@CometCommand
class TwitterCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(user.id)) {
            if (BotVariables.cfg.twitterAccessToken == null) {
                return CometUtil.sendMessage("蓝鸟推送未被正确设置, 请联系机器人管理员")
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
                        if (list.isEmpty()) "没有订阅任何蓝鸟用户".convertToChain() else list.toString().convertToChain()
                    }
                    "push" -> {
                        val cfg = GroupConfigManager.getConfigOrNew(id)
                        cfg.twitterPushEnabled = !cfg.twitterPushEnabled
                        return CometUtil.sendMessage("蓝鸟动态推送已${if (cfg.twitterPushEnabled) "开启" else "关闭"}")
                    }
                    "id" -> {
                        return if (args[1].isNumeric()) {
                            getTweetByID(args[1].toLong(), event.subject)
                        } else {
                            "请输入有效数字".convertToChain()
                        }
                    }
                    else -> getHelp().convertToChain()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("twitter", arrayListOf("twit", "蓝鸟", "tt"), "查询/订阅蓝鸟账号", "nbot.commands.twitter", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /twit info [蓝鸟ID] 查询账号信息
        /twit sub [蓝鸟ID] 订阅用户的推文
        /twit unsub [蓝鸟ID] 取消订阅用户的推文
        /twit push 开启/关闭本群推文推送
        /twit id [推文ID] 通过推文ID查询推文
        
        命令别名: /蓝鸟 /tt /twitter
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        if (super.hasPermission(user, e)) return true
        if (e is GroupMessageEvent && e.sender.permission != MemberPermission.MEMBER) return true
        return false
    }

    @ExperimentalTime
    private suspend fun getTweetToMessageChain(args: List<String>, event: MessageEvent): MessageChain {
        return if (args.size > 1) {
            event.subject.sendMessage(event.message.quote() + CometUtil.sendMessage("正在查询, 请稍等"))
            try {
                if (args.size > 2) getTweetWithDesc(args[1], event.subject, args[2].toInt(), 1 + args[2].toInt())
                else getTweetWithDesc(args[1], event.subject, 0, 1)
            } catch (e: NumberFormatException) {
                CometUtil.sendMessage("请输入有效数字")
            }
        } else {
            getHelp().convertToChain()
        }
    }

    @ExperimentalTime
    private fun getTweetWithDesc(name: String, subject: Contact, index: Int = 1, max: Int = 10): MessageChain {
        return try {
            val tweet = TwitterApi.getTweetInTimeline(name, index, max, false)
            if (tweet != null) {
                return CometUtil.sendMessage("\n${tweet.user.name}\n\n") + tweet.toMessageChain(subject)
            } else {
                CometUtil.sendMessage("获取到的推文为空")
            }
        } catch (t: Throwable) {
            if (NetUtil.isTimeout(t)) {
                CometUtil.sendMessage("获取推文时连接超时")
            } else {
                daemonLogger.warning(t.stackTraceToString())
                CometUtil.sendMessage("获取推文时出现了异常")
            }
        }
    }

    private fun subscribeUser(args: List<String>, groupId: Long): MessageChain {
        if (groupId > 0) {
            val cfg = GroupConfigManager.getConfigOrNew(groupId)
            if (args.size > 1) {
                if (!cfg.twitterSubscribers.contains(args[1])) {
                    val twitter: TwitterUser?

                    try {
                        twitter = TwitterApi.getUserProfile(args[1])
                    } catch (e: RateLimitException) {
                        return CometUtil.sendMessage(e.message)
                    }

                    if (twitter != null) {
                        cfg.twitterSubscribers.add(args[1])
                        return CometUtil.sendMessage("订阅 @${args[1]} 成功")
                    }

                    return CometUtil.sendMessage("订阅 @${args[1]} 失败")
                } else {
                    return CometUtil.sendMessage("已经订阅过 @${args[1]} 了")
                }
            } else {
                return getHelp().convertToChain()
            }
        } else {
            return CometUtil.sendMessage("请填写正确的群号!")
        }
    }

    private fun unsubscribeUser(args: List<String>, groupId: Long): MessageChain {
        if (groupId > 0) {
            val cfg = GroupConfigManager.getConfigOrNew(groupId)
            return if (args.size > 1) {
                if (args[1] == "all" || args[1] == "全部") {
                    cfg.twitterSubscribers.clear()
                    CometUtil.sendMessage("退订全部用户成功")
                } else if (cfg.twitterSubscribers.contains(args[1])) {
                    cfg.twitterSubscribers.remove(args[1])
                    CometUtil.sendMessage("退订 @${args[1]} 成功")
                } else {
                    CometUtil.sendMessage("没有订阅过 @${args[1]}")
                }
            } else {
                getHelp().convertToChain()
            }
        } else {
            return CometUtil.sendMessage("请填写正确的群号!")
        }
    }

    @ExperimentalTime
    private fun getTweetByID(id: Long, target: Contact): MessageChain =
        TwitterApi.getTweetById(id)?.toMessageChain(target)
            ?: PlainText("找不到对应ID的推文").toMessageChain()
}