package io.github.starwishsama.comet.test.util

import io.github.starwishsama.comet.utils.StringUtil.removeTrailingNewline
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("Tests for String Utils")
class StringUtilTest {
    @Nested
    @DisplayName("Tests for removing tailing newline")
    inner class RemoveTailingNewline() {
        @Test
        fun `all newline`() {
            val newStr = "\n\r\n\r\n\r\r\r\n".removeTrailingNewline()
            assertEquals("", newStr)
        }

        @Test
        fun `with text`() {
            val origin = "Never gonna give you up!\n"
            val new = origin.removeTrailingNewline()
            assertEquals("Never gonna give you up!", new)
        }

        @Test
        fun `empty input`() {
            val origin = ""
            val new = origin.removeTrailingNewline()
            assertEquals(origin, new)
        }

        @Test
        fun `different length test index`() {
            val text = "a"
            (1..5).forEach { strLen ->
                (1..3).forEach { trailingLen ->
                    val origin = text.repeat(strLen) + "\n".repeat(trailingLen)
                    val new = origin.removeTrailingNewline()
                    println("[$strLen, $trailingLen]\norigin: '$origin'\nnew: '$new'")
                    assert(!new.contains('\n'))
                    assertEquals(strLen, new.length)
                }
            }
        }

        @Test
        fun `include whitespace`() {
            val orig = "aaaa                       \n\n\n"
            val new = orig.removeTrailingNewline(includeSpace = true)
            assertEquals("aaaa", new)
        }
    }
}
