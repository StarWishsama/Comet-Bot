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
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.PenguinStatService
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object PenguinStatCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        return when (args[0]) {
            "item", "物品" -> {
                PenguinStatService.queryItem(args)
            }
            "update", "更新" -> {
                PenguinStatService.forceUpdate(user)
            }
            else -> {
                getHelp().toChain()
            }
        }
    }

    override val props: CommandProps =
        CommandProps(
            "penguinstats",
            listOf("企鹅物流", "pgs"),
            "查询明日方舟物品掉落信息",

            UserLevel.USER
        )

    override fun getHelp(): String =
        """
        老板~ 欢迎来到企鹅物流数据统计! [Beta]
        
        /pgs item [物品名] 查询该物品在哪里掉落
        /pgs update 强制更新企鹅物流数据
        """.trimIndent()
}
