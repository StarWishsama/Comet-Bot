package ren.natsuyuk1.comet.api.test.event

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.event.*
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.event.events.message.MessageEvent
import kotlin.reflect.full.isSubclassOf
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEventManager {
    @Test
    fun testEquals() {
        assertFalse { MessagePreSendEvent::class.isSubclassOf(MessageEvent::class) }
    }

    class TestEvent: AbstractEvent(), CancelableEvent

    @Test
    fun testEventHandle() {
        EventManager.init()
        registerListener(TestEvent::class) {
            (it as TestEvent).cancel()
        }

        val event = TestEvent()

        runBlocking {
            event.broadcast()
        }

        assertTrue(event.isCancelled)
    }
}
