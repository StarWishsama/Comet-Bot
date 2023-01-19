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
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.api.user.at
import ren.natsuyuk1.comet.objects.config.FeatureConfig
import ren.natsuyuk1.comet.service.HitokotoManager
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.fixDisplay
import java.lang.Double.min
import java.math.RoundingMode
import java.security.SecureRandom
import java.time.ZoneId

fun CometUser.isSigned(): Boolean {
    val checkTime =
        Clock.System.now().toJavaInstant().atZone(ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0)
            .toInstant().toKotlinInstant()

    return checkInDate > checkTime
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

        val at = if (sender is GroupMember) {
            sender.at()
        } else null

        val checkInResult = buildString {
            append("${getCurrentInstantString()}好, ${sender.name}~")
            appendLine()

            val position = getSignInPosition(user)

            if (position == 0) {
                append("今日首位签到")
            } else if (position != -1) {
                append("今日第${position + 1}位签到")
            }

            appendLine()

            if (earnLevel > 0) {
                append("升级! 现在等级为 ${user.level}")
                appendLine()
            }

            if (coinResult.getAllPoint() == 0.0) {
                append("今天运气不佳, 没有硬币 (>_<)")
            } else if (coinResult.basePoint < 0) {
                append("今天运气不佳, 被扣除了 ${coinResult.basePoint.fixDisplay()} 硬币 (>_<)")
            } else {
                append("获得了 ${coinResult.basePoint.fixDisplay()} 点硬币")
            }

            appendLine()

            if (user.checkInTime >= 2) {
                append("连续签到 ${user.checkInTime} 天")
                if (coinResult.awardPoint > 0) append(", 额外获得 ${coinResult.awardPoint.fixDisplay()} 点硬币")
                appendLine()
            }

            if (coinResult.chancePoint > 0) {
                append("随机事件: 额外获得了 ${coinResult.chancePoint.fixDisplay()} 点硬币 (*^_^*)")
                appendLine()
            }

            append("目前硬币 > ${user.coin.fixDisplay()}")
            appendLine()

            append("今日一言 > ${HitokotoManager.getHitokoto()}")
        }

        return buildMessageWrapper {
            at?.let { appendElement(it) }
            appendLine()
            appendText(checkInResult)
        }
    }

    /**
     * 计算签到所得硬币
     *
     * @return 获取硬币情况, 详见 [SignInResult]
     */
    private fun calculate(user: CometUser): Pair<SignInResult, SignInResult> {
        // 计算签到时间
        val currentTime = Clock.System.now()
        val lastSignInTime =
            user.checkInDate.toJavaInstant().atZone(ZoneId.systemDefault()).withHour(0).withMinute(0).withSecond(0)
                .toInstant().toKotlinInstant()
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
            user.checkInDate = currentTime
        }

        // 使用随机数工具生成基础硬币
        val coinBase = NumberUtil.round(
            random.nextDouble(
                FeatureConfig.data.signInSetting.minCoin, FeatureConfig.data.signInSetting.maxCoin
            ),
            1, RoundingMode.HALF_DOWN
        ).toDouble()

        val expBase = NumberUtil.round(
            random.nextDouble(
                FeatureConfig.data.signInSetting.minExp, FeatureConfig.data.signInSetting.maxExp
            ),
            1, RoundingMode.HALF_DOWN
        ).toDouble()

        // 最大奖励倍数
        val awardProp = min(
            FeatureConfig.data.signInSetting.maxAccumulateBonus,
            NumberUtil.round(
                (
                    random.nextDouble(
                        0.0, FeatureConfig.data.signInSetting.accumulateBonus
                    ) * (user.checkInTime - FeatureConfig.data.signInSetting.accumulateBonusStart + 1)
                    ),
                1,
                RoundingMode.HALF_DOWN
            ).toDouble()
        )

        val multiplier = if (coinBase <= 0) 1.0 else awardProp

        // 连续签到的奖励硬币
        val awardPoint = multiplier * coinBase

        val awardExp = multiplier * expBase

        val chancePoint = getRandomEventCoin()

        return Pair(SignInResult(coinBase, awardPoint, chancePoint), SignInResult(expBase, awardExp, 0.0))
    }

    internal data class SignInResult(
        val basePoint: Double,
        val awardPoint: Double,
        val chancePoint: Double
    ) {
        fun getAllPoint(): Double = basePoint + awardPoint + chancePoint
    }

    internal fun getSignInPosition(user: CometUser): Int {
        val checkTime = Clock.System.now()
        val checkLDT = checkTime.toLocalDateTime(TimeZone.currentSystemDefault())

        val before = LocalDateTime(
            checkLDT.year, checkLDT.monthNumber, checkLDT.dayOfMonth, 0, 0, 0, 0
        ).toInstant(TimeZone.currentSystemDefault())
        val after = LocalDateTime(
            checkLDT.year, checkLDT.monthNumber, checkLDT.dayOfMonth, 23, 59, 59, 0
        ).toInstant(TimeZone.currentSystemDefault())

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
            in 5..8 -> "早上"
            in 9..11 -> "上午"
            12 -> "中午"
            in 13..18 -> "下午"
            in 19..22 -> "晚上"
            else -> "凌晨"
        }
    }

    private fun getRandomEventCoin(): Double {
        val randomEvent = random.nextDouble(0.0, 1.0)

        return if (randomEvent <= FeatureConfig.data.signInSetting.randomBonusProbability) {
            random.nextDouble(
                FeatureConfig.data.signInSetting.randomBonusMin, FeatureConfig.data.signInSetting.randomBonusMax
            )
        } else {
            0.0
        }
    }

    private fun getTargetExpDifference(currentLevel: Int): Int = when (currentLevel) {
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

    internal fun levelUp(currentLevel: Int, earnExp: Long): Int {
        var cacheExp = earnExp
        var earnLevel = 0

        while (cacheExp > getTargetExpDifference(currentLevel + earnLevel)) {
            val target = getTargetExpDifference(currentLevel + earnLevel)
            cacheExp -= target
            earnLevel++
        }

        return earnLevel
    }
}
