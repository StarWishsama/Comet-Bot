package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

class RollCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (!BotUtil.isNoCoolDown(user.id)) return EmptyMessageChain

        if (args.size < 2) {
            return getHelp().toMsgChain()
        }

        //@TODO Roll会话
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps(
        "roll",
        arrayListOf("rl", "抽奖"),
        "roll东西",
        "nbot.commands.roll",
        UserLevel.USER
    )

    override fun getHelp(): String = "/roll [需roll的东西] [roll的个数]"
}