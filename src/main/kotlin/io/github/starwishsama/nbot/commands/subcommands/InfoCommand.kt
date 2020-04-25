package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtils.getLocalMessage
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*
import java.time.format.DateTimeFormatter

class InfoCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        try {
            if (args.size == 1) {
                return run {
                    var reply =
                            "\n积分: "+ String.format("%.1f", user.checkInPoint) +
                                    "\n累计连续签到了 " + user.checkInTime.toString() + " 天" + "\n上次签到于: " +
                                    user.lastCheckInTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString() +
                                    "\n权限组: " + user.level.toString() +
                                    "\n命令条数: " + user.randomTime
                    if (user.bindServerAccount != null) {
                        reply = reply + "绑定的游戏账号是: " + user.bindServerAccount
                    }

                    if (message is GroupMessage){
                        message.sender.at() + reply.toMessage().asMessageChain()
                    } else {
                        reply.toMessage().asMessageChain()
                    }
                }
            } else if (args.size == 2 && args[1].contentEquals("排行") || args[1].contentEquals("ph")) {
                val users = BotConstants.users
                users.sortedByDescending { it.checkInPoint }
                val sb = StringBuilder()
                sb.append("积分排行榜").append("\n")
                return if (users.size > 9) {
                    for (i in 0..9) {
                        sb.append(i + 1).append(" ")
                            .append(message.sender.nick)
                            .append(" ").append(String.format("%.1f", users[i].checkInPoint)).append("\n")
                    }
                    (sb.toString().trim { it <= ' ' }).toMessage().asMessageChain()
                } else {
                    "数据不足".toMessage().asMessageChain()
                }
            } else {
                return getHelp().toMessage().asMessageChain()
            }
        } catch (e: Exception) {
            BotInstance.logger.error(e)
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("info", arrayListOf("cx", "查询"), "nbot.commands.info", UserLevel.USER)
    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /cx 查询自己的积分信息
        /cx ph 查询积分排行榜
    """.trimIndent()

}
