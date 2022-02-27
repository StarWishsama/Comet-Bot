package io.github.starwishsama.comet.test.messagewrapper

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class TestMessageWrapper {
    @Test
    fun testMergeString() {
        val testText1 = "Never gonna get you up "
        val testText2 = "Never gonna let you down "
        val testText3 = "Never gonna go around "
        val testText4 = "And desert you"
        val wrapper = MessageWrapper()

        wrapper.addText(testText1)
        wrapper.addText(testText2)
        wrapper.addText(testText3)
        wrapper.addText(testText4)

        assertTrue("Text must be merge! wrapper: ${wrapper.getAllText()}") { testText1+testText2+testText3+testText4 == wrapper.getAllText() }
    }
}