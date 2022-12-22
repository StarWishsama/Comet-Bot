package ren.natsuyuk1.comet.test.network.thirdparty.arcaea

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaClient
import ren.natsuyuk1.comet.service.image.ArcaeaImageService
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print
import ren.natsuyuk1.comet.utils.brotli4j.BrotliLoader
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestArcaeaAPI {
    private val userID = "092799890"

    @BeforeAll
    fun initBrotli() {
        if (isCI()) return

        runBlocking {
            BrotliLoader.loadBrotli()
            ArcaeaClient.fetchConstants()
        }
    }

    @Test
    fun testUserInfoQuery() {
        if (isCI()) return

        runBlocking {
            ArcaeaClient.queryUserInfo(userID)?.getMessageWrapper()?.print()
        }
    }

    @Test
    fun testB30() {
        return

        if (isCI()) return

        runBlocking {
            val (info, result) = ArcaeaClient.queryUserB38(userID, UUID.randomUUID())
            if (info != null) {
                ArcaeaImageService.drawB38(info, result).print()
            }
        }
    }
}
