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

import io.github.starwishsama.comet.BuildConfig

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import kotlin.time.ExperimentalTime


class VersionCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        return ("彗星 Bot " + BuildConfig.version +
                "\n运行时长 ${CometUtil.getRunningTime()}" +
                "\n构建时间: ${BuildConfig.buildTime}" +
                "\nMade with ❤ & Mirai ${BuildConfig.miraiVersion}").convertToChain()
    }

    override fun getProps(): CommandProps {
        return CommandProps(
            "version",
            arrayListOf("v", "版本", "comet"),
            "查看版本号",
            "nbot.commands.version",
            UserLevel.USER
        )
    }

    override fun getHelp(): String = ""
}