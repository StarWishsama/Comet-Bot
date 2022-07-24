package ren.natsuyuk1.comet.test.network.thirdparty.jiki

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.jikipedia.JikiPediaAPI
import ren.natsuyuk1.comet.test.print

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestJikiPediaAPI {
    @Test
    fun testSearch() {
        runBlocking {
            println(JikiPediaAPI.search("叩").toMessageWrapper())
        }
    }

    @Test
    fun testXIDGenerate() {
        JikiPediaAPI.encodeToJikiXID().print()
    }
}
