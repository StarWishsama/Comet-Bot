package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtils
import io.github.starwishsama.nbot.util.MusicUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain

class MusicCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (BotUtils.isNoCoolDown(message.sender.id, 15)){
            return MusicUtil.searchNetEaseMusic(BotUtils.getRestStringInArgs(args, 1))
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("music", arrayListOf("dg", "点歌", "歌"), "nbot.commands.music", UserLevel.USER)
}