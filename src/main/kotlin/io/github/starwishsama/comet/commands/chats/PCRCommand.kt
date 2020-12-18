package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.pool.PCRPool
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.BotUtil.sendMessage
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import org.apache.commons.lang3.StringUtils

@CometCommand
class PCRCommand : ChatCommand {
    // 这只是一个临时测试, 未来将放入卡池链表中
    private val pool = PCRPool()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(user.id, 60)) {
            return if (args.isNotEmpty()) {
                when (args[0]) {
                    "十连" -> pool.getPCRResult(user, 10).sendMessage()
                    "单抽" -> pool.getPCRResult(user, 1).sendMessage()
                    "来一井" -> pool.getPCRResult(user, 300).sendMessage()
                    else -> {
                        if (user.isBotAdmin() || (event is GroupMessageEvent && event.sender.isAdministrator())) {
                            if (StringUtils.isNumeric(args[0])) {
                                val gachaTime: Int = try {
                                    args[0].toInt()
                                } catch (e: NumberFormatException) {
                                    return getHelp().convertToChain()
                                }
                                pool.getPCRResult(user, gachaTime).sendMessage()
                            } else {
                                getHelp().convertToChain()
                            }
                        } else {
                            "次数抽卡功能已停用, 请使用单抽/十连/一井的方式抽卡!".sendMessage()
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