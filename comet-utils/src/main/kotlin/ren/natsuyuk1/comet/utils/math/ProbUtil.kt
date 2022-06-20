package ren.natsuyuk1.comet.utils.math

fun Collection<Int>.partialSum(): List<Int> {
    var sum = 0
    return map { i ->
        (i + sum).also { sum = it }
    }
}

fun Collection<Int>.weightRandom(): Int {
    val ps = partialSum()
    val random = (1..ps.last()).random()
    var l = 0
    var r = ps.size - 1
    while (l < r) {
        val mid = (l + r) ushr 1 // safe from overflows
        if (ps[mid] < random) l = mid + 1
        else r = mid
    }
    return l
}

/**
 * K -> Int
 * Int 为权重
 *
 * @return 抽中的 K
 */
fun <K> Map<K, Int>.weightRandom(): K =
    keys.elementAt(values.weightRandom())
