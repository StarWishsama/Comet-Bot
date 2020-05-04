package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import io.github.starwishsama.nbot.util.DrawUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import org.apache.commons.lang3.StringUtils

class DrawCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ)){
            if (args.isNotEmpty()){
                when (args[0]){
                    "明日方舟", "舟游", "mrfz", "ak" -> {
                        return if (args.size == 2){
                            when (args[1]) {
                                "十连" -> (BotUtil.getLocalMessage("msg.bot-prefix") + DrawUtil.getArkDrawResult(user, 10)).toMirai()
                                "单抽" -> (BotUtil.getLocalMessage("msg.bot-prefix") + DrawUtil.getArkDrawResult(user, 1)).toMirai()
                                else -> {
                                    if (StringUtils.isNumeric(args[1])) {
                                        (BotUtil.getLocalMessage("msg.bot-prefix") + DrawUtil.getArkDrawResult(user, args[1].toInt())).toMirai()
                                    } else {
                                        getHelp().toMirai()
                                    }
                                }
                            }
                        } else {
                            getHelp().toMirai()
                        }
                    }
                    "公主连结", "pcr", "gzlj" -> {
                        return if (args.size == 2){
                            when (args[1]) {
                                "十连" -> (BotUtil.getLocalMessage("msg.bot-prefix") + DrawUtil.getPCRResult(user, 10)).toMirai()
                                "单抽" -> (BotUtil.getLocalMessage("msg.bot-prefix") + DrawUtil.getPCRResult(user, 1)).toMirai()
                                else -> {
                                    if (StringUtils.isNumeric(args[1])) {
                                        (BotUtil.getLocalMessage("msg.bot-prefix") + DrawUtil.getPCRResult(user, args[1].toInt())).toMirai()
                                    } else {
                                        getHelp().toMirai()
                                    }
                                }
                            }
                        } else {
                            return getHelp().toMirai()
                        }
                    }
                    else -> return getHelp().toMirai()
                }
            } else {
                return getHelp().toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("draw", arrayListOf("ck", "抽卡"), "抽卡模拟器", "nbot.commands.draw", UserLevel.USER)

    override fun getHelp(): String = """
         ============ 命令帮助 ============
         /ck mrfz [十连/单抽/次数] 明日方舟抽卡
         /ck pcr [十连/单抽/次数] 公主连结抽卡
    """.trimIndent()
}