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

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.MusicApiType
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.service.command.MusicService
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class MusicCommand : ChatCommand {

    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        return when (args[0]) {
            "api" -> MusicService.setMusicApi(args)

            "mode" -> MusicService.setTextMode()

            MusicApiType.QQ.name -> {
                event.subject.sendMessage("请稍等...")
                MusicService.handleQQMusic(args.getRestString(1), event.subject)
            }

            MusicApiType.NETEASE.name -> {
                event.subject.sendMessage("请稍等...")
                MusicService.handleNetEaseMusic(args.getRestString(1), event.subject)
            }

            else -> {
                event.subject.sendMessage("请稍等...")
                MusicService.handleMusicSearch(args.getRestString(0), event.subject)
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
            consumePoint = 5.0
        )

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /music [歌名] 点歌
        /music api 修改点歌来源
        /music mode 切换显示模式
    """.trimIndent()
}