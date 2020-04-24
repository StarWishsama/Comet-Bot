package io.github.starwishsama.nbot.objects.checkin

import net.mamoe.mirai.contact.Member
import java.time.LocalDateTime

class CheckInData(var startTime: LocalDateTime, var endTime: LocalDateTime, var groupUsers: List<Member>) {
    var checkedUsers = arrayListOf<Member>()
    var lateUsers = arrayListOf<Member>()
}