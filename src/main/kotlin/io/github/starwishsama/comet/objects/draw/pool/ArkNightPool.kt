package io.github.starwishsama.comet.objects.draw.pool

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.items.GachaItem
import io.github.starwishsama.comet.utils.DrawUtil
import java.math.RoundingMode
import java.util.stream.Collectors

/**
 * Operator info from https://amiya.xyz/
 *
 * Operator Picture from http://prts.wiki/
 */
class ArkNightPool(override val name: String = "标准寻访") : GachaPool() {
    override val tenjouCount: Int = -1
    override val tenjouRare: Int = -1
    override val poolItems: MutableList<ArkNightOperator> = BotVariables.arkNight

    override fun doDraw(time: Int): List<ArkNightOperator> {
        val result = mutableListOf<ArkNightOperator>()
        var r6UpRate = 0.00

        repeat(time) {
            // 在五十连之后的保底
            if (it >= 50 && r6UpRate == 0.0) {
                r6UpRate += 0.02
            }

            val probability = RandomUtil.randomDouble(2, RoundingMode.HALF_DOWN)

            // 按默认抽卡规则抽出对应星级干员, 超过五十连后保底机制启用

            val rare: Int =  when (probability) {
                in 0.50..0.52 + r6UpRate -> 6 // 2%
                in 0.0..0.08 -> 5 // 8%
                in 0.09..0.49 -> 4 // 40%
                else -> 3 // 50%
            }

            when {
                // 如果在保底的基础上抽到了六星, 则重置加倍概率
                rare == 6 && r6UpRate > 0 -> r6UpRate = 0.0
                // 究极非酋之后必爆一个六星
                rare != 6 && r6UpRate > 0.5 -> {
                    result.add(getGachaItem(6, r6UpRate))
                    r6UpRate = 0.0
                    return@repeat
                }
            }

            result.add(getGachaItem(rare, probability))
        }
        return result
    }

    override fun getGachaItem(rare: Int): GachaItem {
        throw UnsupportedOperationException("Please use #getGachaItem(Int, Double) instead!")
    }

    // 使用自定义抽卡方式
    private fun getGachaItem(rare: Int, probability: Double): ArkNightOperator {
        // 然后检查是否到了出 UP 角色的时候
        if (highProbabilityItems.isNotEmpty()) {
            val targetUps = highProbabilityItems.keys.parallelStream().collect(Collectors.toList())
            val targetUp = targetUps[RandomUtil.randomInt(0, targetUps.size - 1)]

            val targetProbability = highProbabilityItems[targetUp]

            if (targetProbability != null && probability in targetProbability) {
                return targetUp as ArkNightOperator
            }
        }

        val rareItems = poolItems.parallelStream().filter { it.rare == rare }.collect(Collectors.toList())
        return rareItems[RandomUtil.randomInt(0, rareItems.size)]
    }

    /**
     * 明日方舟抽卡结果
     */
    fun getArkDrawResult(user: BotUser, time: Int = 1): List<ArkNightOperator> {
        return if (DrawUtil.checkHasGachaTime(user, time)) {
            user.decreaseTime(time)
            doDraw(time)
        } else {
            emptyList()
        }
    }

    /**
     * 明日方舟抽卡，返回文字
     */
    fun getArkDrawResultAsString(user: BotUser, time: Int): String {
        val drawResult = getArkDrawResult(user, time)
        if (drawResult.isNotEmpty()) {
            when (time) {
                1 -> {
                    val (name, _, rare) = drawResult[0]
                    return "单次寻访结果\n$name ${DrawUtil.getStar(rare)}"
                }
                10 -> {
                    return StringBuilder("十连寻访结果:\n").apply {
                        for ((name, _, rare) in drawResult) {
                            append(name).append(" ").append(DrawUtil.getStar(rare)).append(" ")
                        }
                    }.trim().toString()
                }
                else -> {
                    val r6Char = drawResult.parallelStream().filter { it.rare == 6 }.collect(Collectors.toList())

                    val r6Text = StringBuilder().apply {
                        r6Char.forEach { append("${it.name} ") }
                    }.toString().trim()

                    return "寻访结果:\n" +
                            "寻访次数: ${drawResult.size}\n" +
                            "六星: ${r6Text}\n" +
                            "五星个数: ${drawResult.parallelStream().filter { it.rare == 5 }.count()}\n" +
                            "四星个数: ${drawResult.parallelStream().filter { it.rare == 4 }.count()}\n" +
                            "三星个数: ${drawResult.parallelStream().filter { it.rare == 3 }.count()}\n\n" +
                            "使用合成玉 ${drawResult.size * 600}"
                }
            }
        } else {
            return DrawUtil.overTimeMessage + "\n剩余次数: ${user.commandTime}"
        }
    }
}