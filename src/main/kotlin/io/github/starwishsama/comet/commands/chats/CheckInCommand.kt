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
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.service.command.CheckInService.handleCheckIn
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

/**
 * [CheckInCommand]
 *
 * 签到命令, 可以获得积分
 *
 * @author StarWishsama
 * @author StivenDing
 */

class CheckInCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        return handleCheckIn(event, user)
    }

    override val props: CommandProps =
        CommandProps("clock", arrayListOf("签到", "qd"), "签到命令", "nbot.commands.clock", UserLevel.USER)

    override fun getHelp(): String = "/qd 签到"
}