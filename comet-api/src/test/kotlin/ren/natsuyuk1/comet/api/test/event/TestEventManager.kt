package ren.natsuyuk1.comet.api.test.event

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.event.impl.comet.MessageSendEvent
import ren.natsuyuk1.comet.api.event.impl.message.MessageEvent
import kotlin.reflect.full.isSubclassOf
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEventManager {
    @Test
    fun testEquals() {
        assertFalse { MessageSendEvent::class.isSubclassOf(MessageEvent::class) }
    }
}
