package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.MusicApi
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtils
import io.github.starwishsama.nbot.util.MusicUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.*

class MusicCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        val api = BotConstants.cfg.musicApi
        if (BotUtils.isNoCoolDown(message.sender.id)){
            return if (args.isNotEmpty()) {
                when (api) {
                    MusicApi.QQ -> {
                        if (BotUtils.isNoCoolDown(message.sender.id, 30)) {
                            MusicUtil.searchQQMusic(BotUtils.getRestStringInArgs(args, 0))
                        } else {
                            EmptyMessageChain
                        }
                    }
                    MusicApi.NETEASE -> MusicUtil.searchNetEaseMusic(BotUtils.getRestStringInArgs(args, 0))
                }
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