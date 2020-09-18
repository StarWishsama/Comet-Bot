package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.annotations.CometCommand
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.DrawUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import org.apache.commons.lang3.StringUtils

@CometCommand
class PCRCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(user.id)) {
            return if (args.isNotEmpty()) {
                when (args[0]) {
                    "十连" -> BotUtil.sendMessage(DrawUtil.getPCRResult(user, 10))
                    "单抽" -> BotUtil.sendMessage(DrawUtil.getPCRResult(user, 1))
                    "来一井" -> return BotUtil.sendMessage(DrawUtil.getPCRResult(user, 300))
                    else -> {
                        if (StringUtils.isNumeric(args[1])) {
                            BotUtil.sendMessage(DrawUtil.getPCRResult(user, args[0].toInt()))
                        } else {
                            getHelp().convertToChain()
                        }
                    }
                }
            } else {
                return getHelp().convertToChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("pcr", arrayListOf("gzlj", "公主连结"), "公主链接抽卡模拟器", "nbot.commands.pcr", UserLevel.USER)

    override fun getHelp(): String = """
         ============ 命令帮助 ============
         /ck pcr [十连/单抽/次数] 公主连结抽卡
    """.trimIndent()
}