/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.commands.service

object SignInService {
    /**fun processSignIn(sender: PlatformCommandSender, user: CometUser): MessageWrapper {
    return if (user.isSigned()) {
    "你今天已经签到过了! 输入 /cx 可查询硬币详情".toMessageWrapper()
    } else {
    signIn(sender, user)
    }
    }

    private fun signIn(sender: PlatformCommandSender, user: CometUser): MessageWrapper {
    val signInCoin = calculateCoin(user)

    val checkInResult = buildString {
    append("${getCurrentInstantString()}好, ${sender.nameOrCard()}~\n")

    val position = getSignInPosition(user)

    if (position == 0) {
    append("今日首位签到\n")
    } else {
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

    if (user.checkInCount >= 2) {
    append("连续签到 ${user.checkInCount} 天 ${if (signInCoin.awardPoint > 0) ", 额外获得 ${signInCoin.awardPoint} 点硬币\n" else "\n"}")
    }

    if (signInCoin.chancePoint > 0) {
    append("随机事件: 额外获得了 ${signInCoin.chancePoint} 点硬币 (*^_^*)\n")
    }

    append("目前硬币 > ${user.coin.fixDisplay()}\n")

    append("今日一言 > ${HitokotoUpdater.getHitokoto()}\n")
    }

    return checkInResult.toMessageChain()
    }

    /**
     * 计算签到所得硬币
     *
     * @return 获取硬币情况, 详见 [CheckInResult]
    */
    private fun calculateCoin(user: CometUser): CheckInResult {
    // 计算签到时间
    val currentTime = LocalDateTime.utcNow()
    val lastSignInTime = user.checkInDate.toLocalDateTime(TimeZone.UTC)
    val checkDuration = Duration.between(, currentTime)

    if (checkDuration.toDays() <= 1) {
    user.plusDay()
    } else {
    user.resetDay()
    }

    user.checkInDateTime = currentTime

    // 使用随机数工具生成基础硬币
    val basePoint = NumberUtil.round(random.nextDouble(0.0, 10.0), 1, RoundingMode.HALF_DOWN).toDouble()

    // 只取小数点后一位，将最大奖励点数限制到 3 倍
    val awardProp =
    min(
    1.5,
    NumberUtil.round((random.nextDouble(0.0, 0.2) * (user.checkInCount - 1)), 1, RoundingMode.HALF_DOWN)
    .toDouble()
    )

    // 连续签到的奖励硬币
    val awardPoint = if (basePoint < 0) {
    0.0
    } else {
    String.format("%.1f", awardProp * basePoint).toDouble()
    }

    val chancePoint = hasRandomEvent()

    user.addPoint(basePoint + awardPoint + chancePoint)

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

    private fun hasRandomEvent(): Double {
    val randomEvent = random.nextDouble(0.0, 1.0)

    return if (randomEvent in 0.49999..0.50001) {
    random.nextDouble(10.0, 25.0)
    } else {
    0.0
    }
    }*/
}