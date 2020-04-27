package io.github.starwishsama.nbot.util

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.objects.draw.ArkNightOperator
import io.github.starwishsama.nbot.objects.draw.PCRCharacter
import java.math.RoundingMode
import java.util.*


object DrawUtil {
    fun tenTimeDrawAr(): List<ArkNightOperator> {
        val ops: MutableList<ArkNightOperator> = ArrayList<ArkNightOperator>()
        for (i in 0..9) {
            ops.add(drawAr())
        }
        return ops
    }

    fun drawAr(): ArkNightOperator {
        val probability = RandomUtil.randomDouble(0.0, 1.0, 2, RoundingMode.HALF_DOWN)
        val rare: Int
        rare = when (probability) {
            in 0.48..0.50 -> 6
            in 0.0..0.08 -> 5
            in 0.40..0.50 -> 4
            else -> 3
        }
        return getOperator(rare)
    }

    fun getOperator(rare: Int): ArkNightOperator {
        val ops: List<ArkNightOperator> = BotConstants.arkNight
        val tempOps: MutableList<ArkNightOperator> = LinkedList<ArkNightOperator>()
        for (op in ops) {
            if (op.rare == rare) {
                tempOps.add(op)
            }
        }
        return tempOps[RandomUtil.randomInt(1, tempOps.size)]
    }

    private const val R3 = 25
    private const val R2 = 200
    private const val R1 = 775

    fun drawPCR(): PCRCharacter {
        val chance = RandomUtil.randomInt(0, R1 + R2 + R3)
        return when {
            chance <= R3 -> {
                getCharacter(3)
            }
            chance <= R2 + R3 -> {
                getCharacter(2)
            }
            else -> {
                getCharacter(1)
            }
        }
    }

    fun tenTimesDrawPCR(): List<PCRCharacter> {
        val result: MutableList<PCRCharacter> = LinkedList<PCRCharacter>()
        for (i in 0..9) {
            result.add(drawPCR())
        }
        for (i in result.indices) {
            if (result[i].star > 2) {
                break
            } else if (i == result.size - 1 && result[i].star < 2) {
                result[i] = getCharacter(2)
            }
        }
        return result
    }

    fun getCharacter(rare: Int): PCRCharacter {
        val temp: MutableList<PCRCharacter> = LinkedList<PCRCharacter>()
        for (c in BotConstants.pcr) {
            if (c.star == rare) {
                temp.add(c)
            }
        }
        return temp[RandomUtil.randomInt(0, 1.coerceAtLeast(temp.size))]
    }
}