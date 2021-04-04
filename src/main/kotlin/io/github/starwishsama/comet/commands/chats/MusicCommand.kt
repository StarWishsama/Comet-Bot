package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.enums.MusicApiType
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.api.thirdparty.music.ThirdPartyMusicApi
import io.github.starwishsama.comet.api.thirdparty.music.entity.MusicSearchResult
import io.github.starwishsama.comet.sessions.Session
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import java.util.*

@CometCommand
class MusicCommand : ChatCommand {
    val api = BotVariables.cfg.musicApi
    val usingUsers = mutableMapOf<Long, List<MusicSearchResult>>()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.size > 1) {
            return when (args[0].toLowerCase()) {
                "api" -> {
                    if (args.size > 2) {
                        when (args[1].toUpperCase()) {
                            "QQ", "TX", "腾讯" -> BotVariables.cfg.musicApi = MusicApiType.QQ
                            "NETEASE", "网易", "WY" -> BotVariables.cfg.musicApi = MusicApiType.NETEASE
                        }
                        toChain("音乐API已修改为 ${BotVariables.cfg.musicApi}")
                    } else {
                        "/music api [API名称] (QQ/WY)".toChain()
                    }
                }
                MusicApiType.QQ.name -> {
                    return handleQQMusic(args.getRestString(1))
                }
                MusicApiType.NETEASE.name -> {
                    return handleNetEaseMusic(args.getRestString(1))
                }
                else -> {
                    return handleMusicSearch(args.getRestString(0))
                }
            }
        } else {
            return getHelp().toChain()
        }
    }

    override fun getProps(): CommandProps =
        CommandProps(
            "music",
            arrayListOf("dg", "点歌", "歌"),
            "点歌命令",
            "nbot.commands.music",
            UserLevel.USER,
            consumePoint = 10
        )

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /music [歌名] 点歌
    """.trimIndent()

    private fun handleMusicSearch(name: String): MessageChain {
        when (api) {
            MusicApiType.NETEASE -> handleNetEaseMusic(name)
            MusicApiType.QQ -> handleQQMusic(name)
        }

        return EmptyMessageChain
    }

    private fun handleNetEaseMusic(name: String): MessageChain {
        try {
            val result = ThirdPartyMusicApi.searchNetEaseMusic(name)

            if (result.isEmpty()) {
                return "❌ 找不到你想搜索的音乐".toChain()
            }

            val song = result[0]

            return MusicShare(
                MusicKind.NeteaseCloudMusic,
                song.name,
                song.getAuthorName(),
                song.jumpURL,
                song.albumPicture,
                song.songURL
            ).toMessageChain()

        } catch (e: Exception) {
            BotVariables.daemonLogger.warning("点歌时出现了意外", e)
            return "❌ 点歌系统发生异常".toChain()
        }
    }

    private fun handleQQMusic(name: String): MessageChain {
        try {
            val result = ThirdPartyMusicApi.searchQQMusic(name)

            if (result.isEmpty()) {
                return "❌ 找不到你想搜索的音乐".toChain()
            }

            val song = result[0]

            return MusicShare(
                MusicKind.QQMusic,
                song.name,
                song.getAuthorName(),
                song.jumpURL,
                song.albumPicture,
                song.songURL
            ).toMessageChain()

        } catch (e: Exception) {
            BotVariables.daemonLogger.warning("点歌时出现了意外", e)
            return "❌ 点歌系统发生异常".toChain()
        }
    }
}