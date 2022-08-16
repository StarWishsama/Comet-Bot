package ren.natsuyuk1.comet.test.network.thirdparty.arcaea

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaClient
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print
import ren.natsuyuk1.comet.utils.brotli4j.BrotliLoader

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestArcaeaAPI {
    private val userID = "092799890"

    @BeforeAll
    fun initBrotli() {
        if (isCI()) return

        runBlocking {
            BrotliLoader.loadBrotli()
        }
    }

    @Test
    fun testUserInfoQuery() {
        if (isCI()) return

        runBlocking {
            ArcaeaClient.queryUserInfo(userID)?.getMessageWrapper()?.print()
        }
    }
}
