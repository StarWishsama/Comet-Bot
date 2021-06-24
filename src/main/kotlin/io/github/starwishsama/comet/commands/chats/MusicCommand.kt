/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.music.ThirdPartyMusicApi
import io.github.starwishsama.comet.enums.MusicApiType
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MusicKind


class MusicCommand : ChatCommand {
    //val usingUsers = mutableMapOf<Long, List<MusicSearchResult>>()
    var plainText: Boolean = false

    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        return when (args[0]) {
            "api" -> {
                if (args.size > 1) {
                    when (args[1].uppercase()) {
                        "QQ", "TX", "腾讯" -> CometVariables.cfg.musicApi = MusicApiType.QQ
                        "NETEASE", "网易", "WY" -> CometVariables.cfg.musicApi = MusicApiType.NETEASE
                    }

                    toChain("音乐API已修改为 ${CometVariables.cfg.musicApi}")
                } else {
                    "/music api [API名称] (QQ/WY)".toChain()
                }
            }

            "mode" -> {
                plainText = !plainText
                "纯文字模式: $plainText".toChain()
            }

            MusicApiType.QQ.name -> {
                event.subject.sendMessage("请稍等...")
                handleQQMusic(args.getRestString(1), event.subject)
            }

            MusicApiType.NETEASE.name -> {
                event.subject.sendMessage("请稍等...")
                handleNetEaseMusic(args.getRestString(1), event.subject)
            }

            else -> {
                event.subject.sendMessage("请稍等...")
                handleMusicSearch(args.getRestString(0), event.subject)
            }
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

    private fun handleMusicSearch(name: String, subject: Contact): MessageChain {
        when (CometVariables.cfg.musicApi) {
            MusicApiType.NETEASE -> handleNetEaseMusic(name, subject)
            MusicApiType.QQ -> handleQQMusic(name, subject)
        }

        return EmptyMessageChain
    }

    private fun handleNetEaseMusic(name: String, subject: Contact): MessageChain {
        try {
            val result = ThirdPartyMusicApi.searchNetEaseMusic(name)

            if (result.isEmpty()) {
                return "❌ 找不到你想搜索的音乐".toChain()
            }

            return if (plainText) {
                result[0].toMessageWrapper().toMessageChain(subject)
            } else {
                result[0].toMessageChain(MusicKind.NeteaseCloudMusic)
            }
        } catch (e: Exception) {
            CometVariables.daemonLogger.warning("点歌时出现了意外", e)
            return "❌ 点歌系统开小差了, 稍后再试试吧".toChain()
        }
    }

    private fun handleQQMusic(name: String, subject: Contact): MessageChain {
        try {
            val result = ThirdPartyMusicApi.searchQQMusic(name)

            if (result.isEmpty()) {
                return "❌ 找不到你想搜索的音乐".toChain()
            }

            return if (plainText) {
                result[0].toMessageWrapper().toMessageChain(subject)
            } else {
                result[0].toMessageChain(MusicKind.QQMusic)
            }
        } catch (e: Exception) {
            CometVariables.daemonLogger.warning("点歌时出现了意外", e)
            return "❌ 点歌系统开小差了, 稍后再试试吧".toChain()
        }
    }
}