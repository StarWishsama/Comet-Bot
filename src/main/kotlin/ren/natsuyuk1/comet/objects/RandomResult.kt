/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects

import io.github.starwishsama.comet.objects.enums.EventRate
import java.text.NumberFormat

data class RandomResult(var id: Long, var chance: Double, var eventName: String) {
    companion object {
        private fun getChance(eventName: String, chance: Double): String {
            val nf = NumberFormat.getPercentInstance()
            val randomResult = "占卜结果 👇\n占卜内容:"
            nf.maximumIntegerDigits = 3
            nf.minimumFractionDigits = 2
            val finalRate = nf.format(chance)
            val rateType = when (chance) {
                in 0.8..1.0 -> EventRate.HIGHEST
                in 0.6..0.8 -> EventRate.HIGH
                in 0.5..0.6 -> EventRate.NORMAL
                in 0.3..0.5 -> EventRate.LOW
                in 0.1..0.3 -> EventRate.LOWEST
                else -> EventRate.NEVER
            }

            return "$randomResult ${eventName}\n结果: ${rateType.type} (${finalRate})"
        }

        fun getChance(result: RandomResult): String {
            val nf = NumberFormat.getPercentInstance()
            nf.maximumIntegerDigits = 3
            nf.minimumFractionDigits = 2
            return getChance(result.eventName, result.chance)
        }
    }
}