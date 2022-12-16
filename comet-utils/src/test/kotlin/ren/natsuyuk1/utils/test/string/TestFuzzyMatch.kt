package ren.natsuyuk1.utils.test.string

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.utils.string.ldSimilarity
import java.math.BigDecimal
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestFuzzyMatch {
    @Test
    fun testLevenshteinDistance() {
        assertEquals(ldSimilarity("dog", "doge"), BigDecimal.valueOf(0.75))
        ldSimilarity("夜に駆ける", "夜駆").also { println(it) }
    }
}
