package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.objects.checkin.ClockInData
import net.mamoe.mirai.contact.Member
import java.time.Duration
import java.time.LocalDateTime

object ClockInManager {
    private val clockInData = mutableMapOf<Long, List<ClockInData>>()

    // 创建一个新的打卡
    fun newClockIn(groupId: Long, members: List<Member>, startTime: LocalDateTime, endTime: LocalDateTime) {
        var data = clockInData[groupId]
        data = if (data == null) {
            mutableListOf(ClockInData(startTime, endTime, members))
        } else {
            data + ClockInData(startTime, endTime, members)
        }

        clockInData[groupId] = data
    }

    // 获取当前时间下时间最近的打卡
    fun getNearestClockIn(groupId: Long): ClockInData? {
        val executeTime = LocalDateTime.now()

        val result = clockInData[groupId]?.parallelStream()?.filter {
            val between = Duration.between(executeTime, it.endTime)
            between.toMinutes() in -5..5 || clockInData[groupId]?.size == 1
        }?.findFirst()

        return result?.get()
    }

    // 检测是否重复打卡
    fun isDuplicate(groupId: Long, duration: Long): Boolean {
        val executeTime = LocalDateTime.now()

        clockInData[groupId]?.forEach {
            if (clockInData[groupId]?.size == 1) {
                return false
            }

            val between = Duration.between(executeTime, it.endTime)

            if (between.toMinutes() < duration && between.toMillis() > -duration) {
                return true
            }
        }

        return false
    }

    fun getClockIn(groupId: Long): List<ClockInData>? {
        return clockInData[groupId]
    }
}