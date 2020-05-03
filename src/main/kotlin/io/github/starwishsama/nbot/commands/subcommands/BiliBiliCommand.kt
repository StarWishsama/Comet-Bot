package io.github.starwishsama.nbot.commands.subcommands

import com.hiczp.bilibili.api.app.model.SearchUserResult
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BiliBiliUtil
import io.github.starwishsama.nbot.util.BotUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage

class BiliBiliCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)){
            if (args.isEmpty()) {
                if (user.biliSubs.isEmpty()) {
                    return getHelp().toMessage().asMessageChain()
                } else {
                    message.quoteReply("请稍等...")
                    val list: MutableList<SearchUserResult.Data.Item> = mutableListOf()
                    for (name in user.biliSubs){
                        if (BiliBiliUtil.searchUser(name).items.isNotEmpty()) {
                            list.add(BiliBiliUtil.searchUser(name).items[0])
                        }
                    }

                    val sb = StringBuilder("监控室列表:\n")

                    if (list.isNotEmpty()){
                        list.sortByDescending { it.liveStatus }
                        for (item in list){
                            sb.append(item.title).append(" ").append( if (item.liveStatus == 1) "✔" else "✘").append("\n")
                        }
                    }

                    return sb.toString().trim().toMessage().asMessageChain()
                }
            } else {
                when (args[0]){
                    "sub", "订阅" -> {
                        if (args.size > 1) {
                            return if (!user.biliSubs.contains(args[1])) {
                                val searchResult = BiliBiliUtil.searchUser(args[1])
                                if (searchResult.items.isNotEmpty()) {
                                    val item = searchResult.items[0]
                                    user.biliSubs += item.title
                                    "Bot > 订阅 ${item.title} 成功".toMessage().asMessageChain()
                                } else {
                                    "Bot > 账号不存在".toMessage().asMessageChain()
                                }
                            } else {
                                "Bot > 你已经订阅过 ${args[1]} 了".toMessage().asMessageChain()
                            }
                        } else {
                            return getHelp().toMessage().asMessageChain()
                        }
                    }
                    "unsub", "取消订阅" -> {
                        return if (args.size > 1) {
                            if (user.biliSubs.isEmpty() || !user.biliSubs.contains(args[1])) {
                                "Bot > 你还没订阅 ${args[1]}".toMessage().asMessageChain()
                            } else {
                                user.biliSubs -= args[1]
                                "Bot > 取消订阅 ${args[1]} 成功".toMessage().asMessageChain()
                            }
                        } else {
                            getHelp().toMessage().asMessageChain()
                        }
                    }
                    "info", "查询" -> {
                        message.quoteReply("请稍等...")
                        val searchResult = BiliBiliUtil.searchUser(args[1])
                        if (searchResult.items.isNotEmpty()) {
                            val item = searchResult.items[0]
                            val before = item.title + "\n粉丝数: " + item.fans +
                                    "\n最近视频: " + (if (!item.avItems.isNullOrEmpty()) item.avItems[0].title else "没有投稿过视频") +
                                    "\n直播状态: " + (if (item.liveStatus == 1) "✔" else "✘")
                            val dynamic = BiliBiliUtil.getDynamic(item.mid)
                            return if (dynamic.isEmpty()) {
                                ("$before\n无最近动态").toMessage().asMessageChain()
                            } else {
                                when (dynamic.size) {
                                    1 -> ("$before\n最近动态: ${dynamic[0]}").toMessage().asMessageChain()
                                    2 -> {
                                        ("$before\n最近动态: ${dynamic[0]}").toMessage().asMessageChain().plus(BotUtil.getImageStream(dynamic[1]).uploadAsImage(message.subject))
                                    }
                                    else -> ("$before\n无最近动态").toMessage().asMessageChain()
                                }
                            }
                        } else {
                            return "Bot > 账号不存在".toMessage().asMessageChain()
                        }
                    }
                    else -> return getHelp().toMessage().asMessageChain()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("bili", arrayListOf(), "nbot.commands.bili", UserLevel.USER)

    override fun getHelp(): String = """
        /bili sub [用户名] 订阅用户相关信息
        /bili unsub [用户名] 取消订阅用户相关信息
        /bili info [用户名] 查看用户的账号详情
    """.trimIndent()

}