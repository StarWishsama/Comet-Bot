package ren.natsuyuk1.utils.test.message

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestMessageWrapper {
    @Test
    fun testSerialization() {
        val message = buildMessageWrapper {
            appendText("Test")
            appendElement(Image(url = "test"))
        }

        println(Json.encodeToString(message))
    }
}
