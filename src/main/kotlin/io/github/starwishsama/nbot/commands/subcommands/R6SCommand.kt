package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.utils.BotUtil
import io.github.starwishsama.nbot.utils.BotUtil.getLocalMessage
import io.github.starwishsama.nbot.utils.BotUtil.isLegitId
import io.github.starwishsama.nbot.utils.BotUtil.isNoCoolDown
import io.github.starwishsama.nbot.utils.R6SUtil.getR6SInfo
import io.github.starwishsama.nbot.utils.toMirai
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.at

class R6SCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (isNoCoolDown(event.sender.id) && event is GroupMessageEvent) {
            if (args.isEmpty()) {
                return (getLocalMessage("msg.bot-prefix") + "/r6s info [Uplay账号名]").toMirai()
            } else {
                when (args[0].toLowerCase()) {
                    "info", "查询" -> {
                        var r6Account = user.r6sAccount
                        if (r6Account == null && args.size > 1 && isLegitId(args[1])) {
                            r6Account = args[1]
                        } else {
                            return ("${getLocalMessage("msg.bot-prefix")} /r6 查询 [ID] 或者 /r6 绑定 [id]\n" +
                                    "绑定彩虹六号账号 无需输入ID快捷查询游戏数据").toMirai()
                        }

                        event.reply(BotUtil.sendMsgPrefix("查询中..."))
                        val result = getR6SInfo(r6Account)
                        return event.sender.at() + ("\n" + result)
                    }
                    "bind", "绑定" ->
                        if (args[1].isNotEmpty() && args.size > 1) {
                            if (isLegitId(args[1])) {
                                val botUser1 = BotUser.getUser(event.sender.id)
                                if (botUser1 != null) {
                                    botUser1.r6sAccount = args[1]
                                    return (getLocalMessage("msg.bot-prefix") + "绑定成功!").toMirai()
                                }
                            } else {
                                return (getLocalMessage("msg.bot-prefix") + "ID 格式有误!").toMirai()
                            }
                        }
                    else -> {
                        return (getLocalMessage("msg.bot-prefix") + "/r6s info [Uplay账号名]").toMirai()
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