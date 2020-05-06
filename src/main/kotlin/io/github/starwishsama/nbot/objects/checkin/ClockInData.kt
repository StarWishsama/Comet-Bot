package io.github.starwishsama.nbot.objects.checkin

import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.MessageChain
import java.time.LocalDateTime

class ClockInData(var startTime: LocalDateTime, var endTime: LocalDateTime, var groupUsers: List<Member>) {
    var checkedUsers = arrayListOf<Member>()
    var lateUsers = arrayListOf<Member>()
    var filterWords = arrayListOf<String>()

    fun addFilterWord(string: String) {
        filterWords.add(string)
    }

    fun viewData(): MessageChain {
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

        return BotUtil.sendMsgPrefix("最近一次打卡的数据:\n已打卡人数: $checkedCount\n迟到: $lateText\n未打卡: $unCheckedText").toMirai()
    }
}