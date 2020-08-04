package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.BotUtil.isLegitId
import io.github.starwishsama.comet.utils.BotUtil.isNoCoolDown
import io.github.starwishsama.comet.utils.R6SUtil.getR6SInfo
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.at
import java.util.*

class R6SCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (isNoCoolDown(event.sender.id) && event is GroupMessageEvent) {
            if (args.isEmpty()) {
                return BotUtil.sendMessage(getHelp(), true)
            } else {
                when (args[0].toLowerCase(Locale.ROOT)) {
                    "info", "查询" -> {
                        val account = user.r6sAccount
                        return if (args.size <= 1 && account != null) {
                            event.reply(BotUtil.sendMsgPrefix("查询中..."))
                            val result = getR6SInfo(account)
                            event.sender.at() + ("\n" + result)
                        } else {
                            if (isLegitId(args[1])) {
                                event.reply(BotUtil.sendMsgPrefix("查询中..."))
                                val result = getR6SInfo(args[1])
                                event.sender.at() + ("\n" + result)
                            } else {
                                BotUtil.sendMessage("你输入的 ID 名不符合育碧用户名规范!")
                            }
                        }
                    }
                    "bind", "绑定" ->
                        if (args[1].isNotEmpty() && args.size > 1) {
                            if (isLegitId(args[1])) {
                                val botUser1 = BotUser.getUser(event.sender.id)
                                if (botUser1 != null) {
                                    botUser1.r6sAccount = args[1]
                                    return BotUtil.sendMessage("绑定成功!")
                                }
                            } else {
                                return BotUtil.sendMessage("ID 格式有误!")
                            }
                        }
                    else -> {
                        return BotUtil.sendMessage("/r6s info [Uplay账号名]")
                    }
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("r6", arrayListOf("r6s", "彩六"), "彩虹六号数据查询", "nbot.commands.r6s", UserLevel.USER)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /r6 info [Uplay账号名] 查询战绩
        /r6 bind [Uplay账号名] 绑定账号
        /r6 info 查询战绩 (需要绑定账号)
    """.trimIndent()
}