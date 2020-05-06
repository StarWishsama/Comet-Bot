package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.commands.interfaces.WaitableCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.Session
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.MessageChain

class ShopCommand : UniversalCommand, WaitableCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        return "WIP".toMirai()
    }

    override fun getProps(): CommandProps =
        CommandProps("shop", arrayListOf("sd", "商店"), "积分商店", "nbot.commands.shop", UserLevel.USER)

    override fun getHelp(): String = """
        /shop list 查看商品列表
        /shop buy 购买商品
        /shop put 上架商品
    """.trimIndent()

    override suspend fun replyResult(message: ContactMessage, user: BotUser, session: Session) {
        message.reply("WIP")
        SessionManager.expireSession(session)
    }
}