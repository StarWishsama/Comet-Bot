package io.github.starwishsama.comet.objects

import io.github.starwishsama.comet.enums.EventRate
import java.text.NumberFormat

data class RandomResult (var id: Long, var chance: Double, var eventName: String){
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

            return "$randomResult ${eventName}\n结果: ${rateType.type} (${finalRate}%)"
        }

        fun getChance(result: RandomResult): String {
            val nf = NumberFormat.getPercentInstance()
            nf.maximumIntegerDigits = 3
            nf.minimumFractionDigits = 2
            return getChance(result.eventName, result.chance)
        }
    }
}