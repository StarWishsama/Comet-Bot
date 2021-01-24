package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.MusicApiType
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.MusicUtil
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.util.*

@CometCommand
class MusicCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        val api = BotVariables.cfg.musicApi
        if (CometUtil.isNoCoolDown(event.sender.id)) {
            if (args.isNotEmpty()) {
                if (args.size > 1) {
                    when (args[0].toLowerCase(Locale.ROOT)) {
                        "api" -> {
                            if (args.size > 1) {
                                when (args[1].toUpperCase()) {
                                    "QQ", "TX", "腾讯" -> BotVariables.cfg.musicApi = MusicApiType.QQ
                                    "NETEASE", "网易", "WY" -> BotVariables.cfg.musicApi = MusicApiType.NETEASE
                                }
                                return CometUtil.sendMessage("音乐API已修改为 ${BotVariables.cfg.musicApi}")
                            }
                        }
                        "QQ", "TX", "腾讯" -> MusicUtil.searchQQMusic(args.getRestString(1), true, event.subject)
                        "NETEASE", "网易", "WY" -> MusicUtil.searchNetEaseMusic(args.getRestString(1))
                        else -> return when (api) {
                            MusicApiType.QQ -> MusicUtil.searchQQMusic(args.getRestString(0), true, event.subject)
                            MusicApiType.NETEASE -> MusicUtil.searchNetEaseMusic(args.getRestString(0))
                        }
                    }
                } else {
                    return when (api) {
                        MusicApiType.QQ -> MusicUtil.searchQQMusic(args.getRestString(0), true, event.subject)
                        MusicApiType.NETEASE -> MusicUtil.searchNetEaseMusic(args.getRestString(0))
                    }
                }
            } else {
                return getHelp().convertToChain()
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