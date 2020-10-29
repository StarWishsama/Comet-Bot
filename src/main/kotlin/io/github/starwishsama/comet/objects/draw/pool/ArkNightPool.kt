package io.github.starwishsama.comet.objects.draw.pool

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.utils.DrawUtil
import java.math.RoundingMode
import java.util.stream.Collectors

/**
 * Picture & Operator info from http://prts.wiki/
 */
class ArkNightPool(override val name: String = "标准寻访") : GachaPool() {
    override val tenjouCount: Int = -1
    override val tenjouRare: Int = -1
    override val poolItems: MutableList<ArkNightOperator> = BotVariables.arkNight

    override fun doDraw(time: Int): List<ArkNightOperator> {
        val result = mutableListOf<ArkNightOperator>()
        var r6Count = 0

        repeat(time) {
            // 在五十连之后的保底
            if (it == 50) {
                r6Count = RandomUtil.randomInt(51, time - 1)
            }

            // 首先检查是否到了出保底六星的时候
            if (r6Count != 0 && it == r6Count) {
                result.add(getGachaItem(6))
                return@repeat
            }

            val probability = RandomUtil.randomDouble(2, RoundingMode.HALF_DOWN)

            // 然后检查是否到了出 UP 角色的时候
            if (highProbabilityItems.isNotEmpty()) {
                val targetUps = highProbabilityItems.keys.parallelStream().collect(Collectors.toList())
                val targetUp = targetUps[RandomUtil.randomInt(0, targetUps.size - 1)]

                val targetProbability = highProbabilityItems[targetUp]

                if (targetProbability != null && probability in targetProbability) {
                    result.add(targetUp as ArkNightOperator)
                    return@repeat
                }
            }

            val rare: Int = when (probability) {
                in 0.48..0.50 -> 6
                in 0.0..0.08 -> 5
                in 0.40..0.90 -> 4
                else -> 3
            }
            result.add(getGachaItem(rare))
        }
        return result
    }

    override fun getGachaItem(rare: Int): ArkNightOperator {
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
                            "三星个数: ${drawResult.parallelStream().filter { it.rare == 3 }.count()}"
                }
            }
        } else {
            return DrawUtil.overTimeMessage
        }
    }
}