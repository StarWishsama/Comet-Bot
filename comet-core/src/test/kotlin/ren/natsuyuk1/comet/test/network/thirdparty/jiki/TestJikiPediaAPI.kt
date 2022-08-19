package ren.natsuyuk1.comet.test.network.thirdparty.jiki

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.jikipedia.JikiPediaAPI
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestJikiPediaAPI {
    @Test
    fun testSearch() {
        if (isCI()) return

        runBlocking {
            JikiPediaAPI.search("叩").toMessageWrapper().print()
        }
    }
}
