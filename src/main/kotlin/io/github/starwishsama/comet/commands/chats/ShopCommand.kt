package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

@CometCommand
class ShopCommand : ChatCommand, SuspendCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        TODO("Shop command's priority is ultra low, sry :/")
    }

    override fun getProps(): CommandProps =
        CommandProps("shop", arrayListOf("sd", "商店"), "积分商店", "nbot.commands.shop", UserLevel.USER)

    override fun getHelp(): String = """
        /shop list 查看商品列表
        /shop buy 购买商品
        /shop put 上架商品
    """.trimIndent()

    override fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        SessionManager.expireSession(session)
    }
}