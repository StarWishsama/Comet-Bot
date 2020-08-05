package io.github.starwishsama.comet.utils

object NumberUtil {
    fun Number?.getBetterNumber(): String {
        if (this == null) return "Unknown"
        if (this !is Int || this !is Long) return toString()

        val length = toString().length
        return when {
            length == 4 -> div(1000.0).toString() + "k"
            length >= 5 -> div(10000.0).toString() + "w"
            else -> toString()
        }
    }
}