/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.clock

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

        val unCheckedUsers = groupUsers.minus(checkedUsers.toSet()).minus(lateUsers.toSet())

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