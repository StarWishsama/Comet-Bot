package io.github.starwishsama.comet.utils

object NumberUtil {
    fun Long.getBetterNumber(): String {
        return when {
            this >= 1000 -> div(1000.0).toString() + "k"
            this >= 10000 -> div(10000.0).toString() + "w"
            else -> toString()
        }
    }
}