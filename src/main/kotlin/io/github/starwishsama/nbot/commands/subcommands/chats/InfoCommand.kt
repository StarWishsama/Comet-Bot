package io.github.starwishsama.nbot.commands.subcommands.chats

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.utils.toMirai
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.at
import java.time.format.DateTimeFormatter

class InfoCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        try {
            if (args.isEmpty()) {
                return run {
                    var reply =
                        "\n积分: " + String.format("%.1f", user.checkInPoint) +
                                "\n累计连续签到了 " + user.checkInTime.toString() + " 天" + "\n上次签到于: " +
                                user.lastCheckInTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString() +
                                "\n权限组: " + user.level.toString() +
                                "\n命令条数: " + user.commandTime

                    if (user.bindServerAccount != null) {
                        reply = reply + "绑定的游戏账号是: " + user.bindServerAccount
                    }

                    if (event is GroupMessageEvent) {
                        event.sender.at() + reply.toMirai()
                    } else {
                        reply.toMirai()
                    }
                }
            } else if (args.size == 1 && args[0].contentEquals("排行") || args[0].contentEquals("ph")) {
                val users = BotConstants.users
                users.sortedByDescending { it.checkInPoint }
                val sb = StringBuilder()
                sb.append("积分排行榜").append("\n")
                return if (users.size > 9) {
                    for (i in 0..9) {
                        sb.append(i + 1).append(" ")
                                .append(users[i].userQQ)
                            .append(" ").append(String.format("%.1f", users[i].checkInPoint)).append("\n")
                    }
                    (sb.toString().trim { it <= ' ' }).toMirai()
                } else {
                    "数据不足".toMirai()
                }
            } else {
                return getHelp().toMirai()
            }
        } catch (e: Exception) {
            BotMain.logger.error(e)
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("info", arrayListOf("cx", "查询"), "查询积分等", "nbot.commands.info", UserLevel.USER)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /cx 查询自己的积分信息
        /cx ph 查询积分排行榜
    """.trimIndent()

}
