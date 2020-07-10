package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.SuspendCommand
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class ShopCommand : UniversalCommand, SuspendCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        return "WIP".toMirai()
    }

    override fun getProps(): CommandProps =
            CommandProps("shop", arrayListOf("sd", "商店"), "积分商店", "nbot.commands.shop", UserLevel.USER)

    override fun getHelp(): String = """
        /shop list 查看商品列表
        /shop buy 购买商品
        /shop put 上架商品
    """.trimIndent()

    override suspend fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        event.reply("WIP")
        SessionManager.expireSession(session)
    }
}