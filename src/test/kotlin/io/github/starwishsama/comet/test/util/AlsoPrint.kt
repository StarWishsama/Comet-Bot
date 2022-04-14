package io.github.starwishsama.comet.test.util

fun <T> T.alsoPrint(): T = this.apply {
    when (this) {
        is DoubleArray -> this.contentToString()
        is FloatArray -> this.contentToString()
        is LongArray -> this.contentToString()
        is IntArray -> this.contentToString()
        is CharArray -> this.contentToString()
        is ShortArray -> this.contentToString()
        is ByteArray -> this.contentToString()
        is BooleanArray -> this.contentToString()
        is Array<*> -> this.contentToString()
        else -> this.toString()
    }.also { println(it) }
}
