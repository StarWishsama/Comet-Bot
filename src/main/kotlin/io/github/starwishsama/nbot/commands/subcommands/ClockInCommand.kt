package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.managers.ClockInManager
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.checkin.ClockInData
import io.github.starwishsama.nbot.utils.BotUtil
import io.github.starwishsama.nbot.utils.toMirai
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ClockInCommand : UniversalCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (event is GroupMessageEvent) {
            val id = event.group.id
            val data = ClockInManager.getNearestClockIn(id)
            return if (data != null) {
                if (isClockIn(data, event)) {
                    clockIn(event.sender, event, data)
                } else {
                    (BotUtil.getLocalMessage("msg.bot-prefix") + "你已经打卡过了!").toMessage()
                            .asMessageChain()
                }
            } else {
                (BotUtil.getLocalMessage("msg.bot-prefix") + "没有正在进行的打卡").toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
            CommandProps("clockin", arrayListOf("打卡", "dk"), "打卡命令", "nbot.commands.clockin", UserLevel.USER)

    override fun getHelp(): String = ""

    private fun isClockIn(data: ClockInData, event: GroupMessageEvent): Boolean {
        if (data.checkedUsers.isNotEmpty()) {
            data.checkedUsers.forEach { member ->
                run {
                    return member.id == event.sender.id
                }
            }
        }
        return false
    }

    private fun clockIn(sender: Member, msg: GroupMessageEvent, data: ClockInData): MessageChain {
        val checkInTime = LocalDateTime.now()
        if (Duration.between(data.endTime, checkInTime).toMinutes() <= 5) {
            var result =
                    "Bot > ${msg.sender.nameCardOrNick} 打卡成功!\n打卡时间: ${checkInTime.format(
                            DateTimeFormatter.ofPattern(
                                    "yyyy-MM-dd HH:mm:ss"
                            )
                    )}"
            result += if (checkInTime.isAfter(data.endTime)) {
                data.lateUsers.add(sender)
                "\n签到状态: 迟到"
            } else {
                "\n签到状态: 成功"
            }

            data.checkedUsers.add(sender)
            return result.toMirai()
        } else {
            return data.viewData()
        }
    }
}