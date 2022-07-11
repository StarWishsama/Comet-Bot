/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.commands.service

import cn.hutool.core.util.NumberUtil
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper
import java.lang.Double.min
import java.math.RoundingMode
import java.security.SecureRandom
import kotlin.time.Duration.Companion.days

fun CometUser.isSigned(): Boolean {
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    return checkInDate.year != currentTime.year
        && checkInDate.month != currentTime.month
        && checkInDate.dayOfMonth != currentTime.dayOfMonth
        && checkInDate.hour >= 8
}

object SignInService {
    private val random = SecureRandom()

    fun processSignIn(user: CometUser, sender: PlatformCommandSender): MessageWrapper {
        return if (user.isSigned()) {
            "你今天已经签到过了! 输入 /cx 可查询硬币详情".toMessageWrapper()
        } else {
            signIn(sender, user)
        }
    }

    private fun signIn(sender: PlatformCommandSender, user: CometUser): MessageWrapper {
        val signInCoin = calculateCoin(user)

        val checkInResult = buildString {
            append("${getCurrentInstantString()}好, ${sender.name}~\n")

            val position = getSignInPosition(user)

            if (position == 0) {
                append("今日首位签到\n")
            } else if (position != -1) {
                append("今日第${position + 1}位签到\n")
            }

            if (signInCoin.getAllPoint() == 0.0) {
                append("今天运气不佳, 没有硬币 (>_<)")
            } else if (signInCoin.basePoint < 0) {
                if (user.coin - signInCoin.basePoint < 0 || user.coin <= 0) {
                    append("今天运气不佳, 但你的硬币快不够扣了, 就算了吧 o(￣▽￣)ｄ")
                } else {
                    append("今天运气不佳, 被扣除了 ${signInCoin.basePoint.fixDisplay()} 硬币 (>_<)")
                }
            } else {
                append("获得了 ${signInCoin.basePoint.fixDisplay()} 点硬币")
            }

            append("\n")

            if (user.checkInTime >= 2) {
                append("连续签到 ${user.checkInTime} 天 ${if (signInCoin.awardPoint > 0) ", 额外获得 ${signInCoin.awardPoint} 点硬币\n" else "\n"}")
            }

            if (signInCoin.chancePoint > 0) {
                append("随机事件: 额外获得了 ${signInCoin.chancePoint} 点硬币 (*^_^*)\n")
            }

            append("目前硬币 > ${user.coin.fixDisplay()}\n")

            //append("今日一言 > ${HitokotoUpdater.getHitokoto()}\n")
        }

        return checkInResult.toMessageWrapper()
    }

    /**
     * 计算签到所得硬币
     *
     * @return 获取硬币情况, 详见 [CheckInResult]
     */
    private fun calculateCoin(user: CometUser): CheckInResult {
        // 计算签到时间
        val currentTime = Clock.System.now()
        val lastSignInTime = user.checkInDate.toInstant(TimeZone.UTC)
        val checkDuration = currentTime - lastSignInTime

        if (checkDuration.inWholeDays <= 1) {
            transaction {
                user.checkInTime += 1
            }
        } else {
            transaction {
                user.checkInTime = 1
            }
        }

        transaction {
            user.checkInDate = currentTime.toLocalDateTime(TimeZone.UTC)
        }

        // 使用随机数工具生成基础硬币
        val basePoint = NumberUtil.round(random.nextDouble(0.0, 10.0), 1, RoundingMode.HALF_DOWN).toDouble()

        // 只取小数点后一位，将最大奖励点数限制到 3 倍
        val awardProp = min(
            1.5,
            NumberUtil.round((random.nextDouble(0.0, 0.2) * (user.checkInTime - 1)), 1, RoundingMode.HALF_DOWN)
                .toDouble()
        )

        // 连续签到的奖励硬币
        val awardPoint = if (basePoint < 0) {
            0.0
        } else {
            String.format("%.1f", awardProp * basePoint).toDouble()
        }

        val chancePoint = hasRandomEvent()

        transaction {
            user.coin += (basePoint + awardPoint + chancePoint)
        }

        return CheckInResult(basePoint, awardPoint, chancePoint)
    }

    private data class CheckInResult(
        val basePoint: Double,
        val awardPoint: Double,
        val chancePoint: Double,
    ) {
        fun getAllPoint(): Double = basePoint + awardPoint + chancePoint
    }

    private fun getSignInPosition(user: CometUser): Int {
        val checkTime = Clock.System.now()
        val checkLDT = checkTime.toLocalDateTime(TimeZone.UTC)

        /**
         * 签到刷新时间为 UTC +8 0:00
         */
        val before = LocalDateTime(
            checkLDT.year,
            checkLDT.monthNumber, checkTime.minus(1.days).toLocalDateTime(TimeZone.UTC).dayOfMonth, 8, 0, 0, 0
        )
        val after = LocalDateTime(
            checkLDT.year,
            checkLDT.monthNumber, checkTime.plus(1.days).toLocalDateTime(TimeZone.UTC).dayOfMonth, 8, 0, 0, 0
        )

        return transaction {
            val sortedByDate = UserTable.select {
                UserTable.checkInDate greater before
                UserTable.checkInDate lessEq after
            }

            sortedByDate.sortedBy { it[UserTable.checkInDate] }.indexOfFirst { it[UserTable.id] == user.id }
        }
    }

    private fun getCurrentInstantString(): String {
        val timeNow = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Hong_Kong"))

        return when (timeNow.hour) {
            in 6..8 -> "早上"
            in 9..11 -> "上午"
            12 -> "中午"
            in 13..18 -> "下午"
            in 19..22 -> "晚上"
            else -> "凌晨"
        }
    }

    private fun hasRandomEvent(): Double {
        val randomEvent = random.nextDouble(0.0, 1.0)

        return if (randomEvent in 0.499999..0.500001) {
            random.nextDouble(50.0, 100.0)
        } else {
            0.0
        }
    }
}
