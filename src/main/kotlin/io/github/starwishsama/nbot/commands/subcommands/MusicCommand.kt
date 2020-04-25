package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtils
import io.github.starwishsama.nbot.util.MusicUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.*

class MusicCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (BotUtils.isNoCoolDown(message.sender.id, 15)){
            return if (args.isNotEmpty()) {
                MusicUtil.searchNetEaseMusic(BotUtils.getRestStringInArgs(args, 0))
            } else {
                getHelp().toMessage().asMessageChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("music", arrayListOf("dg", "点歌", "歌"), "nbot.commands.music", UserLevel.USER)
    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /music [歌名] 点歌
    """.trimIndent()
}