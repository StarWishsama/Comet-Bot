package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.enums.MusicApi
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.BotUtil.getRestString
import io.github.starwishsama.comet.utils.MusicUtil
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

class MusicCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        val api = BotVariables.cfg.musicApi
        if (BotUtil.isNoCoolDown(event.sender.id)) {
            if (args.isNotEmpty()) {
                if (args[0].contentEquals("api")) {
                    if (args.size > 1) {
                        when (args[1].toUpperCase()) {
                            "QQ" -> BotVariables.cfg.musicApi = MusicApi.QQ
                            "NETEASE", "网易" -> BotVariables.cfg.musicApi = MusicApi.NETEASE
                        }
                        return BotUtil.sendMsgPrefix("音乐API已修改为 ${BotVariables.cfg.musicApi}").toMsgChain()
                    }
                } else {
                    return when (api) {
                        MusicApi.QQ -> MusicUtil.searchQQMusic(args.getRestString(0))
                        MusicApi.NETEASE -> MusicUtil.searchNetEaseMusic(args.getRestString(0))
                    }
                }
            } else {
                getHelp().toMsgChain()
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