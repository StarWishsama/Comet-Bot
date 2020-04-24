package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtils
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
                    return clockIn(message.sender, message).toMessage().asMessageChain()
                } else {
                    data.checkedUsers.forEach { member ->
                        run {
                            return if (member.id == message.sender.id) {
                                (BotUtils.getLocalMessage("msg.bot-prefix") + "你已经打卡过了!").toMessage()
                                    .asMessageChain()
                            } else {
                                clockIn(message.sender, message).toMessage().asMessageChain()
                            }
                        }
                    }
                }
            } else {
                return (BotUtils.getLocalMessage("msg.bot-prefix") + "没有正在进行的打卡").toMessage().asMessageChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("clockin", arrayListOf("打卡", "dk"), "nbot.commands.clockin", UserLevel.USER)

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
            BotConstants.checkInCalendar.remove(msg.group.id)
            return "签到已过期"
        }
    }
}