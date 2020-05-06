package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BiliBiliUtil
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.isNumeric
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadAsImage

class BiliBiliCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)) {
            if (args.isEmpty()) {
                return getHelp().toMirai()
            } else {
                when (args[0]) {
                    "sub", "订阅" -> {
                        if (args.size > 1) {
                            return if (user.isBotAdmin()) {
                                if (args[1].isNumeric()) {
                                    BotConstants.cfg.subList.add(args[1].toLong())
                                    BotUtil.sendMsgPrefix("订阅房间号 ${args[1]} 成功").toMirai()
                                } else {
                                    val searchResult = BiliBiliUtil.searchUser(args[1])
                                    if (searchResult.items.isNotEmpty()) {
                                        val item = searchResult.items[0]
                                        BotConstants.cfg.subList.add(item.roomid)
                                        BotUtil.sendMsgPrefix("订阅 ${item.title} 成功").toMirai()
                                    } else {
                                        BotUtil.sendMsgPrefix("账号不存在").toMirai()
                                    }
                                }
                            } else {
                                BotUtil.sendMsgPrefix("你已经订阅过直播间 ${args[1]} 了").toMirai()
                            }
                        } else {
                            return getHelp().toMirai()
                        }
                    }
                    "unsub", "取消订阅" -> {
                        if (args.size > 1) {
                            var roomId = 0L
                            if (args[1].isNumeric()) {
                                roomId = args[1].toLong()
                            } else {
                                val searchResult = BiliBiliUtil.searchUser(args[1])
                                if (searchResult.items.isNotEmpty()) {
                                    val item = searchResult.items[0]
                                    roomId = item.mid
                                }
                            }

                            return if (BotConstants.cfg.subList.isEmpty() || !BotConstants.cfg.subList.contains(roomId)) {
                                BotUtil.sendMsgPrefix("你还没订阅直播间 ${args[1]}").toMirai()
                            } else {
                                BotConstants.cfg.subList.remove(args[1].toLong())
                                BotUtil.sendMsgPrefix("取消订阅直播间 ${args[1]} 成功").toMirai()
                            }
                        } else {
                            getHelp().toMirai()
                        }
                    }
                    "info", "查询" -> {
                        event.quoteReply("请稍等...")
                        val searchResult = BiliBiliUtil.searchUser(args[1])
                        if (searchResult.items.isNotEmpty()) {
                            val item = searchResult.items[0]
                            val before = item.title + "\n粉丝数: " + item.fans +
                                    "\n最近视频: " + (if (!item.avItems.isNullOrEmpty()) item.avItems[0].title else "没有投稿过视频") +
                                    "\n直播状态: " + (if (item.liveStatus == 1) "✔" else "✘")
                            val dynamic = BiliBiliUtil.getDynamic(item.mid)
                            return if (dynamic.isEmpty()) {
                                ("$before\n无最近动态").toMirai()
                            } else {
                                when (dynamic.size) {
                                    1 -> ("$before\n最近动态: ${dynamic[0]}").toMirai()
                                    2 -> ("$before\n最近动态: ${dynamic[0]}").toMirai()
                                        .plus(BotUtil.getImageStream(dynamic[1]).uploadAsImage(event.subject))
                                    else -> ("$before\n无最近动态").toMirai()
                                }
                            }
                        } else {
                            return "Bot > 账号不存在".toMirai()
                        }
                    }
                    else -> return getHelp().toMirai()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("bili", arrayListOf(), "订阅B站主播/查询用户动态", "nbot.commands.bili", UserLevel.USER)

    override fun getHelp(): String = """
        /bili sub [用户名] 订阅用户相关信息
        /bili unsub [用户名] 取消订阅用户相关信息
        /bili info [用户名] 查看用户的动态
    """.trimIndent()

}