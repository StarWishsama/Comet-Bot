/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.managers

import io.github.starwishsama.comet.objects.clock.ClockInData
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