package io.github.starwishsama.comet.test.util

import io.github.starwishsama.comet.utils.StringUtil.removeTrailingNewline
import io.github.starwishsama.comet.utils.StringUtil.toHex
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

    @Nested
    @DisplayName("Tests for hex convert")
    inner class HexConvert() {
        @Test
        fun `to hex little endian`() {
            val hex = "114514".toHex(lineSize = 8).alsoPrint()
            assertEquals("31 31 34 35 31 34 00 00", hex)
        }
        @Test
        fun `to hex multiple line`() {
            val orig = "😂🤔🤣🤣😱🏃🏊🈶🈚こんにちは  こんばんは"
            val hex1 = orig.toHex(lineSize = 13).alsoPrint()
            assertEquals("""
                f0 9f 98 82 f0 9f a4 94 f0 9f a4 a3 f0
                9f a4 a3 f0 9f 98 b1 f0 9f 8f 83 f0 9f
                8f 8a f0 9f 88 b6 f0 9f 88 9a e3 81 93
                e3 82 93 e3 81 ab e3 81 a1 e3 81 af 20
                20 e3 81 93 e3 82 93 e3 81 b0 e3 82 93
                e3 81 af 00 00 00 00 00 00 00 00 00 00
            """.trimIndent(), hex1)

            val hex2 = orig.toHex(lineSize = 13, padding = false).alsoPrint()
            assertEquals("""
                f0 9f 98 82 f0 9f a4 94 f0 9f a4 a3 f0
                9f a4 a3 f0 9f 98 b1 f0 9f 8f 83 f0 9f
                8f 8a f0 9f 88 b6 f0 9f 88 9a e3 81 93
                e3 82 93 e3 81 ab e3 81 a1 e3 81 af 20
                20 e3 81 93 e3 82 93 e3 81 b0 e3 82 93
                e3 81 af
            """.trimIndent(), hex2)
        }
    }
}
