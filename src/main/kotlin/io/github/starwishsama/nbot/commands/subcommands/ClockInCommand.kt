package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ClockInCommand : UniversalCommand{
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (message is GroupMessage){
            val id = message.group.id
            if (BotConstants.checkInCalendar.containsKey(id)){
                val data = BotConstants.checkInCalendar[id]
                if (data?.checkedUsers?.isEmpty()!!){
                    return clockIn(message.sender, message).toMirai()
                } else {
                    data.checkedUsers.forEach { member ->
                        run {
                            return if (member.id == message.sender.id) {
                                (BotUtil.getLocalMessage("msg.bot-prefix") + "你已经打卡过了!").toMessage()
                                    .asMessageChain()
                            } else {
                                clockIn(message.sender, message).toMirai()
                            }
                        }
                    }
                }
            } else {
                return (BotUtil.getLocalMessage("msg.bot-prefix") + "没有正在进行的打卡").toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("clockin", arrayListOf("打卡", "dk"), "nbot.commands.clockin", UserLevel.USER)
    override fun getHelp(): String = ""

    private fun clockIn(sender: Member, msg: GroupMessage): String {
        val data = BotConstants.checkInCalendar[msg.group.id]
        val checkInTime = LocalDateTime.now()
        if (checkInTime.minusMinutes(5).isBefore(data?.endTime)) {
            var result =
                "Bot > ${msg.sender.nameCardOrNick}, 签到成功!\n签到时间: ${checkInTime.format(
                    DateTimeFormatter.ofPattern(
                        "yyyy-MM-dd HH:mm:ss"
                    )
                )}"
            result += if (checkInTime.isAfter(data?.endTime)) {
                data?.lateUsers?.add(sender)
                data?.checkedUsers?.add(sender)
                "\n签到状态: 迟到"
            } else {
                data?.checkedUsers?.add(sender)
                "\n签到状态: 成功"
            }
            return result
        } else {
            val checkedCount = data?.checkedUsers?.size
            var lateText = StringBuilder()
            var unCheckedText = StringBuilder()

            val unChecked = data?.groupUsers?.minus(data.checkedUsers)?.minus(data.lateUsers)

            unChecked?.forEach { member ->
                run {
                    unCheckedText.append(member.nameCardOrNick).append(",")
                }
                unCheckedText.removeSuffix(",")
            }

            data?.lateUsers?.forEach { member ->
                run {
                    lateText.append(member.nameCardOrNick).append(",")
                }
                lateText.removeSuffix(",")
            }

            if (lateText.toString().isEmpty()) {
                lateText = StringBuilder("无")
            }

            if (unCheckedText.toString().isEmpty()) {
                unCheckedText = StringBuilder("无")
            }

            BotConstants.checkInCalendar.remove(msg.group.id)
            return "${BotUtil.getLocalMessage("msg.bot-prefix")}打卡已关闭\n已打卡人数: $checkedCount\n迟到: $lateText\n未打卡: $unCheckedText"
        }
    }
}