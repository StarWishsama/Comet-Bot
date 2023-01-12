package ren.natsuyuk1.comet.utils.string

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

/**
 * The Levenshtein distance between two words is the minimum number of
 * single-character edits (insertions, deletions or substitutions) required to
 * change one string into the other.
 *
 * @author Thibault Debatty
 * @see https://github.com/tdebatty/java-string-similarity#levenshtein
 */
fun levenshteinDistance(s1: String, s2: String, limit: Int = Int.MAX_VALUE): Double {
    if (s1 == s2) {
        return 0.0
    }

    if (s1.isBlank()) {
        return s2.length.toDouble()
    }

    if (s2.isBlank()) {
        return s1.length.toDouble()
    }

    // create two work vectors of integer distances
    var v0 = IntArray(s2.length + 1)
    var v1 = IntArray(s2.length + 1)
    var vtemp: IntArray

    // initialize v0 (the previous row of distances)
    // this row is A[0][i]: edit distance for an empty s
    // the distance is just the number of characters to delete from t

    // initialize v0 (the previous row of distances)
    // this row is A[0][i]: edit distance for an empty s
    // the distance is just the number of characters to delete from t
    for (i in v0.indices) {
        v0[i] = i
    }

    for (i in s1.indices) {
        // calculate v1 (current row distances) from the previous row v0
        // first element of v1 is A[i+1][0]
        //   edit distance is delete (i+1) chars from s to match empty t
        v1[0] = i + 1
        var minv1 = v1[0]

        // use formula to fill in the rest of the row
        for (j in s2.indices) {
            var cost = 1
            if (s1.toCharArray()[i] == s2.toCharArray()[j]) {
                cost = 0
            }
            v1[j + 1] = // Cost of insertion
                // Cost of remove
                (v1[j] + 1).coerceAtMost((v0[j + 1] + 1).coerceAtMost(v0[j] + cost)) // Cost of substitution
            minv1 = minv1.coerceAtMost(v1[j + 1])
        }
        if (minv1 >= limit) {
            return limit.toDouble()
        }

        // copy v1 (current row) to v0 (previous row) for next iteration
        // System.arraycopy(v1, 0, v0, 0, v0.length);

        // Flip references to current and previous row
        vtemp = v0
        v0 = v1
        v1 = vtemp
    }

    return v0[s2.length].toDouble()
}

fun ldSimilarity(s1: String, s2: String): BigDecimal {
    val ld = levenshteinDistance(s1, s2)
    return BigDecimal.ONE.minus(
        BigDecimal.valueOf(ld).divide(BigDecimal.valueOf(max(s1.length, s2.length).toLong()), 2, RoundingMode.HALF_UP)
    )
}
