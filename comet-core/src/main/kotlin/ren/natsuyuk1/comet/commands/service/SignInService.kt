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
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.service.HitokotoManager
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import java.lang.Double.min
import java.math.RoundingMode
import java.security.SecureRandom

fun CometUser.isSigned(): Boolean {
    val currentTime = Clock.System.now()
    return (currentTime - checkInDate.toInstant(TimeZone.currentSystemDefault())).inWholeDays < 1
}

object SignInService {
    private val random = SecureRandom()

    suspend fun processSignIn(user: CometUser, sender: PlatformCommandSender): MessageWrapper {
        return if (user.isSigned()) {
            "你今天已经签到过了! 输入 /info 可查询详情".toMessageWrapper()
        } else {
            signIn(sender, user)
        }
    }

    private suspend fun signIn(sender: PlatformCommandSender, user: CometUser): MessageWrapper {
        val (coinResult, expResult) = calculate(user)
        val earnLevel = levelUp(user.level, expResult.getAllPoint().toLong())

        transaction {
            user.exp += expResult.getAllPoint().toLong()
            user.coin += coinResult.getAllPoint()
            user.level += earnLevel
        }

        val checkInResult = buildString {
            append("${getCurrentInstantString()}好, ${sender.name}~\n")

            val position = getSignInPosition(user)

            if (position == 0) {
                append("今日首位签到")
            } else if (position != -1) {
                append("今日第${position + 1}位签到")
            }

            append("\n")

            if (earnLevel > 0) {
                append("升级! 现在等级为 ${user.level}")
                append("\n")
            }

            if (coinResult.getAllPoint() == 0.0) {
                append("今天运气不佳, 没有硬币 (>_<)")
            } else if (coinResult.basePoint < 0) {
                append("今天运气不佳, 被扣除了 ${coinResult.basePoint.fixDisplay()} 硬币 (>_<)")
            } else {
                append("获得了 ${coinResult.basePoint.fixDisplay()} 点硬币")
            }

            append("\n")

            if (user.checkInTime >= 2) {
                append("连续签到 ${user.checkInTime} 天 ${if (coinResult.awardPoint > 0) ", 额外获得 ${coinResult.awardPoint} 点硬币\n" else "\n"}")
            }

            if (coinResult.chancePoint > 0) {
                append("随机事件: 额外获得了 ${coinResult.chancePoint} 点硬币 (*^_^*)\n")
            }

            append("目前硬币 > ${user.coin.fixDisplay()}\n")

            append("今日一言 > ${HitokotoManager.getHitokoto()}\n")
        }

        return checkInResult.toMessageWrapper()
    }

    /**
     * 计算签到所得硬币
     *
     * @return 获取硬币情况, 详见 [SignInResult]
     */
    private fun calculate(user: CometUser): Pair<SignInResult, SignInResult> {
        // 计算签到时间
        val currentTime = Clock.System.now()
        val lastSignInTime = user.checkInDate.toInstant(TimeZone.currentSystemDefault())
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
            user.checkInDate = currentTime.toLocalDateTime(TimeZone.currentSystemDefault())
        }

        // 使用随机数工具生成基础硬币
        val coinBase = NumberUtil.round(random.nextDouble(0.0, 3.0), 1, RoundingMode.HALF_DOWN).toDouble()

        val expBase = NumberUtil.round(random.nextDouble(0.0, 10.0), 1, RoundingMode.HALF_DOWN).toDouble()

        // 只取小数点后一位，将最大奖励点数限制到 1.5 倍
        val awardProp = min(
            1.5,
            NumberUtil.round((random.nextDouble(0.0, 0.2) * (user.checkInTime - 1)), 1, RoundingMode.HALF_DOWN)
                .toDouble()
        )

        // 连续签到的奖励硬币
        val awardPoint = if (coinBase < 0) {
            0.0
        } else {
            String.format("%.1f", awardProp * coinBase).toDouble()
        }

        val awardExp = if (coinBase < 0) {
            0.0
        } else {
            String.format("%.1f", awardProp * coinBase).toDouble()
        }

        val chancePoint = getRandomEventCoin()

        return Pair(SignInResult(coinBase, awardPoint, chancePoint), SignInResult(expBase, awardExp, 0.0))
    }

    private data class SignInResult(
        val basePoint: Double,
        val awardPoint: Double,
        val chancePoint: Double,
    ) {
        fun getAllPoint(): Double = basePoint + awardPoint + chancePoint
    }

    fun getSignInPosition(user: CometUser): Int {
        val checkTime = Clock.System.now()
        val checkLDT = checkTime.toLocalDateTime(TimeZone.currentSystemDefault())

        val before = LocalDateTime(
            checkLDT.year,
            checkLDT.monthNumber,
            checkLDT.dayOfMonth,
            0,
            0,
            0,
            0
        )
        val after = LocalDateTime(
            checkLDT.year,
            checkLDT.monthNumber,
            checkLDT.dayOfMonth,
            23,
            59,
            59,
            0
        )

        return transaction {
            val sortedByDate = UserTable.select {
                UserTable.checkInDate greaterEq before and (UserTable.checkInDate lessEq after)
            }

            sortedByDate.sortedBy { it[UserTable.checkInDate] }.indexOfFirst { it[UserTable.id] == user.id }
        }
    }

    private fun getCurrentInstantString(): String {
        val timeNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        return when (timeNow.hour) {
            in 6..8 -> "早上"
            in 9..11 -> "上午"
            12 -> "中午"
            in 13..18 -> "下午"
            in 19..22 -> "晚上"
            else -> "凌晨"
        }
    }

    private fun getRandomEventCoin(): Double {
        val randomEvent = random.nextDouble(0.0, 1.0)

        return if (randomEvent in 0.499999..0.500001) {
            random.nextDouble(50.0, 100.0)
        } else {
            0.0
        }
    }

    private fun getTargetExp(currentLevel: Int): Int =
        when (currentLevel) {
            in 0..15 -> {
                2 * currentLevel + 7
            }

            in 16..30 -> {
                5 * currentLevel - 38
            }

            else -> {
                9 * currentLevel - 158
            }
        }

    private fun levelUp(currentLevel: Int, earnExp: Long): Int {
        val targetExp = getTargetExp(currentLevel)

        return if (earnExp > targetExp) {
            var earnLevel = 1

            while (true) {
                if (earnExp > getTargetExp(currentLevel + earnLevel)) {
                    earnLevel++
                } else {
                    break
                }
            }

            earnLevel
        } else {
            0
        }
    }
}
