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
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import kotlinx.coroutines.delay
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.utils.MiraiExperimentalApi
import java.time.format.DateTimeFormatter

@OptIn(MiraiExperimentalApi::class)

class InfoCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            val reply =
                "\n积分: " + String.format("%.1f", user.checkInPoint) +
                        "\n累计连续签到了 " + user.checkInTime.toString() + " 天" + "\n上次签到于: " +
                        user.lastCheckInTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString() +
                        "\n权限组: " + user.level.toString()

            return if (event is GroupMessageEvent) {
                event.sender.at() + reply.convertToChain()
            } else {
                reply.convertToChain()
            }
        } else if (args.size == 1 && args[0].contentEquals("排行") || args[0].contentEquals("ph")) {
            val users = CometVariables.cometUsers.values.sortedByDescending { it.checkInPoint }
            val sb = StringBuilder()
            sb.append("积分排行榜").append("\n")
            return if (users.size > 9) {
                for (i in 0..9) {
                    sb.append(i + 1).append(" ")
                        .append(Mirai.queryProfile(event.bot, users[i].id).nickname)
                        .append(" ").append(String.format("%.1f", users[i].checkInPoint)).append("\n")
                }
                delay(500)
                (sb.toString().trim { it <= ' ' }).convertToChain()
            } else {
                "数据不足, 请等待系统更新".convertToChain()
            }
        } else {
            return getHelp().convertToChain()
        }
    }

    override val props: CommandProps =
        CommandProps("info", arrayListOf("cx", "查询"), "查询积分等", UserLevel.USER)

    override fun getHelp(): String = """
        /cx 查询自己的积分信息
        /cx ph 查询积分排行榜
    """.trimIndent()

}
