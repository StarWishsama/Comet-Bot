package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.MusicApi
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.utils.BotUtil
import io.github.starwishsama.nbot.utils.BotUtil.getRestString
import io.github.starwishsama.nbot.utils.MusicUtil
import io.github.starwishsama.nbot.utils.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

class MusicCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        val api = BotConstants.cfg.musicApi
        if (BotUtil.isNoCoolDown(event.sender.id)) {
            if (args.isNotEmpty()) {
                if (args[0].contentEquals("api")) {
                    if (args.size > 1) {
                        when (args[1].toUpperCase()) {
                            "QQ" -> {
                                BotConstants.cfg.musicApi = MusicApi.QQ
                                return BotUtil.sendMsgPrefix("音乐API已修改为QQ").toMirai()
                            }
                            "NETEASE", "网易" -> {
                                BotConstants.cfg.musicApi = MusicApi.NETEASE
                                return BotUtil.sendMsgPrefix("音乐API已修改为网易").toMirai()
                            }
                        }
                    }
                } else {
                    return when (api) {
                        MusicApi.QQ -> {
                            if (BotUtil.isNoCoolDown(event.sender.id, 30)) {
                                MusicUtil.searchQQMusic(args.getRestString(0))
                            } else {
                                EmptyMessageChain
                            }
                        }
                        MusicApi.NETEASE -> MusicUtil.searchNetEaseMusic(args.getRestString(0))
                    }
                }
            } else {
                getHelp().toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("music", arrayListOf("dg", "点歌", "歌"), "点歌命令", "nbot.commands.music", UserLevel.USER)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /music [歌名] 点歌
    """.trimIndent()
}