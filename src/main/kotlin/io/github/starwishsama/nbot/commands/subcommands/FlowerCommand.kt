package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.commands.interfaces.WaitableCommand
import io.github.starwishsama.nbot.enums.SessionType
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.Flower
import io.github.starwishsama.nbot.sessions.Session
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage

class FlowerCommand : UniversalCommand, WaitableCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()){
            return getHelp().toMessage().asMessageChain()
        } else {
            when (args[0]) {
                "lq", "get", "领取" -> {
                    return if (user.flower == null) {
                        SessionManager.addSession(Session(0, SessionType.DELAY, this))
                        BotUtil.sendLocalMessage("msg.bot-prefix", "请给你的花取个名吧").toMessage().asMessageChain()
                    } else {
                        BotUtil.sendLocalMessage("msg.bot-prefix", "你已经种植了 ${user.flower?.flowerName}").toMessage().asMessageChain()
                    }
                }
                "sj", "collect", "收集" -> {
                    BotUtil.sendLocalMessage("msg.bot-prefix", "没写完").toMessage().asMessageChain()
                }
                "cx", "info", "查询" -> {
                    BotUtil.sendLocalMessage("msg.bot-prefix", "没写完").toMessage().asMessageChain()
                }
                else -> return getHelp().toMessage().asMessageChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("flower", arrayListOf("hy", "花园"), "nbot.commands.flower", UserLevel.USER)

    override fun getHelp(): String = """
        /hy lq 领取你的绿植
        /hy sj 收集产出积分
        /hy cx 查询绿植状态
    """.trimIndent()

    override suspend fun replyResult(message: ContactMessage, user: BotUser, session: Session) {
        if (user.flower == null) {
            user.flower = Flower(message.message.contentToString())
            message.reply(BotUtil.sendLocalMessage("msg.bot-prefix", "成功种植 ${user.flower?.flowerName}").toMessage().asMessageChain())
        }
    }

}