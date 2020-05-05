package io.github.starwishsama.nbot.objects.checkin

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChain
import java.time.LocalDateTime

class CheckInData(var startTime: LocalDateTime, var endTime: LocalDateTime, var groupUsers: List<Member>) {
    var checkedUsers = arrayListOf<Member>()
    var lateUsers = arrayListOf<Member>()

    fun unregister(groupId: Long): MessageChain {
        val checkedCount = checkedUsers.size
        var lateText = StringBuilder()
        var unCheckedText = StringBuilder()

        val unChecked = groupUsers.minus(checkedUsers).minus(lateUsers)

        unChecked.forEach { member ->
            run {
                unCheckedText.append(member.nameCardOrNick).append(",")
            }
            unCheckedText.removeSuffix(",")
        }

        lateUsers.forEach { member ->
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

        BotConstants.checkInCalendar.remove(groupId)
        return BotUtil.sendMsgPrefix("打卡已关闭\n已打卡人数: $checkedCount\n迟到: $lateText\n未打卡: $unCheckedText").toMirai()
    }
}