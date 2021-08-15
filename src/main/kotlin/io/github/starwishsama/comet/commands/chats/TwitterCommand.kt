/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.CometVariables.daemonLogger

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.api.thirdparty.twitter.data.TwitterUser
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.managers.NetworkRequestManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.api.TwitterConfig
import io.github.starwishsama.comet.objects.tasks.network.INetworkRequestTask
import io.github.starwishsama.comet.objects.tasks.network.NetworkRequestTask
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
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

class TwitterCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (ApiManager.getConfig<TwitterConfig>().token.isEmpty()) {
            return toChain("推特推送未被正确设置, 请联系机器人管理员")
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
                    return toChain("推特动态推送已${if (cfg.twitterPushEnabled) "开启" else "关闭"}")
                }
                "id" -> {
                    return if (args[1].isNumeric()) {
                        val task = object : NetworkRequestTask(), INetworkRequestTask<MessageChain> {
                            override fun request(param: String): MessageChain {
                                return getTweetByID(args[1].toLong(), event.subject)
                            }

                            override val content: Contact = event.subject
                            override val param: String = ""

                            override fun callback(result: Any?) {
                                if (result is MessageChain) {
                                    runBlocking {
                                        content.sendMessage(result)
                                    }
                                }
                            }

                        }

                        NetworkRequestManager.addTask(task)

                        EmptyMessageChain
                    } else {
                        "请输入有效数字".convertToChain()
                    }
                }
                "nopic" -> {
                    val cfg = GroupConfigManager.getConfigOrNew(id)
                    cfg.twitterPictureMode = !cfg.twitterPictureMode
                    return toChain("推特动态推送图片已${if (cfg.twitterPushEnabled) "开启" else "关闭"}")
                }
                else -> getHelp().convertToChain()
            }
        }
    }

    override val props: CommandProps =
        CommandProps("data", arrayListOf("twit", "推特", "tt"), "查询/订阅推特账号", "nbot.commands.data", UserLevel.ADMIN)

    override fun getHelp(): String = """
        /twit info [推特ID] 查询账号信息
        /twit sub [推特ID] 订阅用户的推文
        /twit unsub [推特ID] 取消订阅用户的推文
        /twit push 开启/关闭本群推文推送
        /twit id [推文ID] 通过推文ID查询推文
        /twit list 查询订阅列表
        /twit nopic 开启无图模式
        
        命令别名: /推特 /tt /twitter
    """.trimIndent()

    override fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
        if (super.hasPermission(user, e)) return true
        if (e is GroupMessageEvent && e.sender.permission != MemberPermission.MEMBER) return true
        return false
    }

    @ExperimentalTime
    private suspend fun getTweetToMessageChain(args: List<String>, event: MessageEvent): MessageChain {
        return if (args.size > 1) {
            val task = object : NetworkRequestTask(), INetworkRequestTask<MessageChain> {
                override fun request(param: String): MessageChain {
                    return if (args.size > 2) {
                        val index = args[2].toIntOrNull()

                        if (index != null) {
                            getTweetWithDesc(args[1], event.subject, index, 1 + index)
                        } else {
                            "请输入有效的数字".toChain()
                        }
                    } else {
                        getTweetWithDesc(args[1], event.subject, 0, 1)
                    }
                }

                override val content: Contact = event.subject
                override val param: String = ""

                override fun callback(result: Any?) {
                    if (result is MessageChain) {
                        runBlocking {
                            content.sendMessage(result)
                        }
                    }
                }

            }

            NetworkRequestManager.addTask(task)

            event.message.quote() + toChain("正在查询, 请稍等")
        } else {
            getHelp().convertToChain()
        }
    }

    @ExperimentalTime
    private fun getTweetWithDesc(name: String, subject: Contact, index: Int = 1, max: Int = 10): MessageChain {
        return try {
            val tweet = TwitterApi.getTweetInTimeline(name, index, max)
            if (tweet != null) {
                return "\n${tweet.user.name}".toChain() + "\n\n" + tweet.toMessageChain(subject)
            } else {
                toChain("获取到的推文为空")
            }
        } catch (t: Throwable) {
            if (NetUtil.isTimeout(t)) {
                toChain("获取推文时连接超时")
            } else {
                daemonLogger.warning(t)
                toChain("获取推文时出现了异常")
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
                        return toChain("订阅 @${args[1]} 失败")
                    }

                    cfg.twitterSubscribers.add(args[1])
                    return toChain("订阅 ${twitterUser.name}(@${twitterUser.twitterId}) 成功")
                } else {
                    return toChain("已经订阅过 @${args[1]} 了")
                }
            } else {
                return getHelp().convertToChain()
            }
        } else {
            return toChain("请填写正确的群号!")
        }
    }

    private fun unsubscribeUser(args: List<String>, groupId: Long): MessageChain {
        if (groupId > 0) {
            val cfg = GroupConfigManager.getConfigOrNew(groupId)
            return if (args.size > 1) {
                if (args[1] == "all" || args[1] == "全部") {
                    cfg.twitterSubscribers.clear()
                    toChain("退订全部用户成功")
                } else if (cfg.twitterSubscribers.contains(args[1])) {
                    cfg.twitterSubscribers.remove(args[1])
                    toChain("退订 @${args[1]} 成功")
                } else {
                    toChain("没有订阅过 @${args[1]}")
                }
            } else {
                getHelp().convertToChain()
            }
        } else {
            return toChain("请填写正确的群号!")
        }
    }

    @ExperimentalTime
    private fun getTweetByID(id: Long, target: Contact): MessageChain =
        TwitterApi.getTweetById(id)?.toMessageChain(target)
            ?: PlainText("找不到对应ID的推文").toMessageChain()
}