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
import io.github.starwishsama.comet.service.command.JikiPediaService
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class JikiPediaCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        return JikiPediaService.searchJiki(event, args.getRestString(0))
    }

    override val props: CommandProps = CommandProps(
        "jiki",
        listOf("小鸡词典", "jikipedia"),
        "在小鸡词典上搜索",
        "nbot.commands.jiki",
        UserLevel.USER,
        consumePoint = 10.0
    )

    override fun getHelp(): String = """
        /jiki [关键词] 搜索对应词可能的意思
        
        数据源来自小鸡词典 🐥 
    """.trimIndent()
}