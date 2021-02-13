package io.github.starwishsama.comet.objects.checkin

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import java.time.LocalDateTime

data class ClockInData(val startTime: LocalDateTime, val endTime: LocalDateTime, private var groupUsers: List<Member>) {
    var checkedUsers = arrayListOf<Member>()
    var lateUsers = arrayListOf<Member>()

    /**
     * 查看打卡数据
     */
    fun viewData(): MessageWrapper {
        val checkedCount = checkedUsers.size
        var lateText = StringBuilder()
        var unCheckedText = StringBuilder()

        val unCheckedUsers = groupUsers.minus(checkedUsers).minus(lateUsers)

        unCheckedUsers.forEach { member ->
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

        return MessageWrapper().addText("最近一次打卡的数据:\n已打卡人数: $checkedCount\n迟到: $lateText\n未打卡: $unCheckedText")
    }
}