package ren.natsuyuk1.comet.test.commands.service.arcaea

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaClient
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaHelper
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print
import ren.natsuyuk1.comet.utils.brotli4j.BrotliLoader
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestArcaeaAPI {
    private val userID = "092799890"

    @BeforeAll
    fun initBrotli() {
        if (isCI()) return

        runBlocking {
            BrotliLoader.loadBrotli()
            SkikoHelper.findSkikoLibrary()
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
        if (isCI()) return

        runBlocking {
            val (info, result) = ArcaeaClient.queryUserB30(userID, UUID.randomUUID())
            if (info != null) {
                ArcaeaHelper.drawB30(info, result).print()
            }
        }
    }
}
