package io.github.starwishsama.comet.test.messagewrapper

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture
import io.github.starwishsama.comet.objects.wrapper.PureText
import io.github.starwishsama.comet.objects.wrapper.buildMessageWrapper
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

        assertTrue("Text must be merge! wrapper: ${wrapper.parseToString()}") {
            testText1 + testText2 + testText3 + testText4 == wrapper.parseToString()
        }
    }

    fun testMultipleElements() {
        val text = PureText("Good night, ")
        val text2 = PureText("Kizuna AI. ")
        val image = Picture("https://i2.hdslb.com/bfs/face/478c8351dc6046e32993f8b03a0d566ffb395ff1.jpg")
        val text3 = PureText(" Wish could see you again :)")

        val wrapper = buildMessageWrapper {
            addElements(text, text2, image, text3)
        }

        assertTrue("Some element had losted! wrapper: ${wrapper.getMessageContent()}") {
            text.text + text2.text + "[图片]" + text3.text == wrapper.toMessageChain().contentToString()
        }
    }
}