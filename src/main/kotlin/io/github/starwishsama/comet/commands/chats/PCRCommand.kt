package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.pool.PCRPool
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import org.apache.commons.lang3.StringUtils

@CometCommand
class PCRCommand : ChatCommand {
    // 这只是一个临时测试, 未来将放入卡池链表中
    private val pool = PCRPool()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(user.id)) {
            return if (args.isNotEmpty()) {
                when (args[0]) {
                    "十连" -> BotUtil.sendMessage(pool.getPCRResult(user, 10))
                    "单抽" -> BotUtil.sendMessage(pool.getPCRResult(user, 1))
                    "来一井" -> return BotUtil.sendMessage(pool.getPCRResult(user, 300))
                    else -> {
                        if (StringUtils.isNumeric(args[0])) {
                            BotUtil.sendMessage(pool.getPCRResult(user, args[0].toInt()))
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
         /pcr [十连/单抽/次数] 公主连结抽卡
    """.trimIndent()
}