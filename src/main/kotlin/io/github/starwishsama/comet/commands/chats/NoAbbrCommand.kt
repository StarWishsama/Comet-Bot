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

import io.github.starwishsama.comet.api.command.CommandExecuteConsumerType
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.NoAbbrService
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class NoAbbrCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        return NoAbbrService.parseAbbr(event, args.getRestString(0))
    }

    override val props: CommandProps =
        CommandProps(
            "noabbr",
            listOf("nbnhhsh", "hhsh", "解析缩写", "能不能好好说话"),
            "能不能好好说话?",
            "nbot.commands.noabbr",
            UserLevel.USER,
            consumerType = CommandExecuteConsumerType.COOLDOWN,
            consumePoint = 15.0,
        )

    override fun getHelp(): String = """
/noabbr [缩写] 解析缩写的意思
        
能不能好好说话?        
    """.trimIndent()
}