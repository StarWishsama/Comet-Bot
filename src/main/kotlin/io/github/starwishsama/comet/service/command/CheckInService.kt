/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.command

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.tasks.HitokotoUpdater
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.NumberUtil.fixDisplay
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.min

/**
 * [CheckInService]
 *
 * 负责处理签到命令 [CheckInCommand]
 */
object CheckInService {
    fun handleCheckIn(event: MessageEvent, user: CometUser): MessageChain {
        return if (user.isChecked()) {
            "你今天已经签到过了! 输入 /cx 可查询签到信息".toChain()
        } else {
            doCheckIn(event, user)
        }
    }

    private fun doCheckIn(event: MessageEvent, user: CometUser): MessageChain {
        val checkInPoint = calculatePoint(user)
        val sender = event.sender

        val checkInResult = buildString {
            append("${getCurrentInstantString()}好, ${sender.nameCardOrNick}~\n")

            val position = getCheckInPosition(user)

            if (position == 0) {
                append("今日首位签到\n")
            } else {
                append("今日第${position + 1}位签到\n")
            }

            if (checkInPoint.getAllPoint() == 0.0) {
                append("今天运气不佳, 没有积分 (>_<)")
            } else if (checkInPoint.basePoint < 0) {
                if (user.checkInPoint - checkInPoint.basePoint < 0 || user.checkInPoint <= 0) {
                    append("今天运气不佳, 但你的积分快不够扣了, 就算了吧 o(￣▽￣)ｄ")
                } else {
                    append("今天运气不佳, 被扣除了 ${checkInPoint.basePoint.fixDisplay()} 积分 (>_<)")
                }
            } else {
                append("获得了 ${checkInPoint.basePoint.fixDisplay()} 点积分")
            }

            append("\n")

            if (user.checkInCount >= 2) {
                append("连续签到 ${user.checkInCount} 天 ${if (checkInPoint.extraPoint > 0) ", 幸运获得了 ${checkInPoint.extraPoint} 点积分~\n" else "\n"}")
            }

            append("目前积分 > ${user.checkInPoint.fixDisplay()}\n")

            append("今日一言 > ${HitokotoUpdater.getHitokoto(false)}\n")
        }

        return checkInResult.toChain()
    }

    /**
     * 计算签到所得积分
     *
     * @return 获取积分情况, 详见 [CheckInResult]
     */
    private fun calculatePoint(user: CometUser): CheckInResult {
        // 计算签到时间
        val currentTime = LocalDateTime.now()
        val checkDuration = Duration.between(user.checkInDateTime, currentTime)

        if (checkDuration.toDays() <= 1) {
            user.plusDay()
        } else {
            user.resetDay()
        }

        user.checkInDateTime = currentTime

        // 使用随机数工具生成基础积分
        val basePoint = RandomUtil.randomDouble(-1.0, 10.0, 1, RoundingMode.HALF_DOWN)

        // 只取小数点后一位，将最大奖励点数限制到 3 倍
        val awardProp =
            min(1.5, (RandomUtil.randomDouble(0.0, 0.2, 1, RoundingMode.HALF_DOWN) * (user.checkInCount - 1)))

        // 连续签到的奖励积分
        val awardPoint = if (basePoint < 0) {
            0.0
        } else {
            String.format("%.1f", awardProp * basePoint).toDouble()
        }

        user.addPoint(basePoint + awardPoint)

        return CheckInResult(basePoint, awardPoint)
    }

    private data class CheckInResult(
        val basePoint: Double,
        val extraPoint: Double
    ) {
        fun getAllPoint(): Double = basePoint + extraPoint
    }

    private fun getCheckInPosition(user: CometUser): Int {
        val checkTime = LocalDateTime.now()
        val sortedByDate = CometVariables.cometUsers.filter {
            it.value.checkInDateTime.dayOfMonth == checkTime.dayOfMonth
                    && it.value.checkInDateTime.dayOfYear == checkTime.dayOfYear
                    && it.value.checkInDateTime.dayOfWeek == checkTime.dayOfWeek
        }.entries.sortedBy { it.value.checkInDateTime }

        return sortedByDate.indexOfFirst { it.value == user }
    }

    private fun getCurrentInstantString(): String {
        val timeNow = LocalDateTime.now()

        return when (timeNow.hour) {
            in 6..8 -> "早上"
            in 9..11 -> "上午"
            12 -> "中午"
            in 13..18 -> "下午"
            in 19..22 -> "晚上"
            else -> "凌晨"
        }
    }
}