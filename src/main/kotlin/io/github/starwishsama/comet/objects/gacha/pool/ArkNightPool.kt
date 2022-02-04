/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.gacha.pool

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.gacha.impl.ArkNightInstance
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.gacha.GachaResult
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.items.GachaItem
import io.github.starwishsama.comet.utils.GachaUtil
import java.math.RoundingMode
import java.util.stream.Collectors

/**
 * [ArkNightPool]
 *
 * 明日方舟抽卡实现方法
 *
 * 干员信息来源: https://github.com/Kengxxiao/ArknightsGameData/
 *
 * 干员图片来源: http://prts.wiki/
 *
 * 感谢以上大佬提供的资源!
 *
 * @author StarWishsama
 */

class ArkNightPool(
    override val name: String = "标准寻访",
    override val displayName: String = "标准寻访",
    override val description: String = "适合多种场合的强力干员",
    override val startTime: Long = -1,
    override val endTime: Long = -1,
    val condition: ArkNightOperator.() -> Boolean = {
        GachaUtil.hasOperator(this.name) && obtain?.contains("招募寻访") == true
    },
) : GachaPool() {
    override val tenjouCount: Int = -1
    override val tenjouRare: Int = -1
    override val poolItems: MutableList<ArkNightOperator> = mutableListOf()

    init {
        ArkNightInstance.getArkNightOperators().stream().filter { condition(it) }.forEach {
            poolItems.add(it)
        }

        CometVariables.daemonLogger.log(
            HinaLogLevel.Info,
            message = "已加载了 ${poolItems.size} 个干员至卡池 $name",
            prefix = "明日方舟"
        )
    }

    private val r3Range = 0.0..0.5
    private val r4Range = 0.5..0.9
    private val r5Range = 0.9..0.98

    override fun doDraw(time: Int): GachaResult {
        val gachaResult = GachaResult()
        var r6UpRate = 0.00

        repeat(time) {
            // 在五十连之后的保底
            if (it >= 50 && r6UpRate == 0.0) {
                r6UpRate += 0.02
            }

            // 按默认抽卡规则抽出对应星级干员, 超过五十连后保底机制启用
            val rare: Int = when (RandomUtil.randomDouble(2, RoundingMode.HALF_DOWN)) {
                in r3Range -> 3 // 50%
                in r4Range -> 4 // 40%
                in r5Range -> 5 // 8%
                else -> 6 // 2%
            }

            when {
                // 如果在保底的基础上抽到了六星, 则重置加倍概率
                r6UpRate > 0 -> {
                    if (rare == 6) {
                        r6UpRate = 0.0
                    } else {
                        r6UpRate += 0.02
                    }
                }

                // 究极非酋之后必爆一个六星
                rare != 6 && r6UpRate >= 1 -> {
                    getArkNightOperator(rare).apply {
                        gachaResult.items.add(operator)
                    }
                    r6UpRate = 0.0
                    return@repeat
                }
            }

            getArkNightOperator(rare).apply {
                if (isSpecial) {
                    gachaResult.specialItems.add(operator)
                }

                gachaResult.items.add(operator)
            }
        }
        return gachaResult
    }

    override fun getGachaItem(rare: Int): GachaItem {
        return getArkNightOperator(rare).operator
    }

    private data class ArkNightOperatorInfo(
        val operator: ArkNightOperator,
        val isSpecial: Boolean
    )

    // 使用自定义抽卡方式
    @Suppress("UNCHECKED_CAST")
    private fun getArkNightOperator(rare: Int): ArkNightOperatorInfo {
        // 首先获取所有对应星级干员
        val rareItems = poolItems.parallelStream().filter { it.rare == (rare - 1) }.collect(Collectors.toList())

        require(rareItems.isNotEmpty()) { "获取干员列表失败: 空列表. 等级为 ${rare - 1}, 池内干员数 ${poolItems.size}" }

        var weight: Int

        // 然后检查是否到了出 UP 角色的时候
        if (highProbabilityItems.isNotEmpty()) {
            val highItems =
                highProbabilityItems.keys.parallelStream().collect(Collectors.toList()) as MutableList<ArkNightOperator>
            rareItems.addAll(highItems.filter { it.rare == rare - 1 })

            rareItems.shuffle()

            weight = RandomUtil.randomInt(0, rareItems.size - 1)

            val target = rareItems[weight]

            for ((gi, prop) in highProbabilityItems) {
                val recalculatedProp = (weight * prop).toInt()
                val redrawResult = rareItems[recalculatedProp]

                if (redrawResult.name == gi.name) {
                    return ArkNightOperatorInfo(redrawResult, true)
                } else {
                    weight = RandomUtil.randomInt(0, rareItems.size - 1)
                    rareItems.shuffle()
                }
            }

            return ArkNightOperatorInfo(target, false)
        } else {
            weight = RandomUtil.randomInt(0, rareItems.size - 1)
            rareItems.shuffle()
            val operator = rareItems[weight]
            return ArkNightOperatorInfo(operator, false)
        }
    }

    /**
     * 明日方舟抽卡结果
     */
    fun getArkDrawResult(user: CometUser, time: Int = 1): GachaResult {
        return if (GachaUtil.checkHasGachaTime(user, time)) {
            user.consumePoint(time * 0.05)
            doDraw(time)
        } else {
            GachaResult()
        }
    }

    /**
     * 明日方舟抽卡，返回文字
     */
    @Suppress("UNCHECKED_CAST")
    fun getArkDrawResultAsString(user: CometUser, drawResult: GachaResult): String {
        val currentPool = "目前卡池为: $displayName\n"
        if (!drawResult.isEmpty()) {
            when (drawResult.items.size) {
                1 -> {
                    val (name, _, rare) = drawResult.items[0] as ArkNightOperator
                    return currentPool + "单次寻访结果\n$name ${GachaUtil.getStarText(rare + 1)}"
                }
                10 -> {
                    return StringBuilder(currentPool + "十连寻访结果:\n").apply {
                        for ((name, _, rare) in drawResult.items as List<ArkNightOperator>) {
                            append(name).append(" ").append(GachaUtil.getStarText(rare + 1)).append(" ")
                        }
                    }.trim().toString()
                }
                else -> {
                    val r6Count = drawResult.items.count { it.rare + 1 == 6 }
                    val r5Count = drawResult.items.count { it.rare + 1 == 4 }
                    val r4Count = drawResult.items.count { it.rare + 1 == 3 }
                    val r3Count = drawResult.items.size - r6Count - r5Count - r4Count
                    val perTimeUsed = 600

                    var returnText = currentPool + "寻访结果:\n" +
                            "寻访次数: ${drawResult.items.size}\n" +
                            "结果: ${r6Count}[6]|${r5Count}[5]|${r4Count}[4]|${r3Count}[3]\n" +
                            "使用合成玉 ${drawResult.items.size * perTimeUsed}"

                    if (drawResult.specialItems.isNotEmpty()) {
                        returnText += "\n\n出现特殊物品!\n"
                        drawResult.specialItems.forEach {
                            returnText += "${it.name} > ${GachaUtil.getStarText(it.rare + 1)}"
                        }
                    }

                    returnText += "\n\n本次消费硬币 ${drawResult.items.size * 0.05}"

                    return returnText
                }
            }
        } else {
            return GachaUtil.overTimeMessage + "\n剩余硬币: ${user.coin}"
        }
    }

    /**
     * 明日方舟抽卡，返回文字
     */
    fun getArkDrawResultAsString(user: CometUser, time: Int): String =
        getArkDrawResultAsString(user, getArkDrawResult(user, time))
}