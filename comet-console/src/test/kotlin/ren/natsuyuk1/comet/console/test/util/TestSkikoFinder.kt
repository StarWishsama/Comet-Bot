package ren.natsuyuk1.comet.console.test.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.console.util.SkikoFinder

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSkikoFinder {
    @Test
    fun testFinder() {
        runBlocking {
            SkikoFinder.findSkikoLibrary()
        }
    }
}
