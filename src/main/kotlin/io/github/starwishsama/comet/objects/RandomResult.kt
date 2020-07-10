package io.github.starwishsama.comet.objects

import io.github.starwishsama.comet.enums.EventRate
import java.text.NumberFormat


data class RandomResult (var id: Long, var chance: Double, var eventName: String){
    companion object {
        private fun getChance(eventName: String, chance: Double): String {
            val nf = NumberFormat.getPercentInstance()
            nf.maximumIntegerDigits = 3
            nf.minimumFractionDigits = 2
            val finalRate = nf.format(chance)
            return if (chance > 0.8 && chance <= 1.0) {
                "结果是" + EventRate.HIGHEST.type + " (" + finalRate + "), 今天非常适合" + eventName + "!"
            } else if (chance > 0.6 && chance <= 0.8) {
                "结果是" + EventRate.HIGH.type + " (" + finalRate + "), 今天很适合" + eventName + "!"
            } else if (chance > 0.5 && chance <= 0.6) {
                "结果是" + EventRate.NORMAL.type + " (" + finalRate + "), 今天适合" + eventName + "!"
            } else if (chance > 0.3 && chance <= 0.5) {
                "结果是" + EventRate.LOW.type + " (" + finalRate + "), 今天不太适合" + eventName + "..."
            } else if (chance > 0.1 && chance <= 0.3) {
                "结果是" + EventRate.LOWEST.type + " (" + finalRate + "), 今天最好不要" + eventName + "..."
            } else if (chance <= 0.1) {
                "结果是" + EventRate.NEVER.type + " (" + finalRate + "), 千万别" + eventName + "!"
            } else {
                "你要占卜的东西有点怪呢, 我无法占卜出结果哦."
            }
        }

        fun getChance(result: RandomResult): String {
            val nf = NumberFormat.getPercentInstance()
            nf.maximumIntegerDigits = 3
            nf.minimumFractionDigits = 2
            return getChance(result.eventName, result.chance)
        }
    }
}