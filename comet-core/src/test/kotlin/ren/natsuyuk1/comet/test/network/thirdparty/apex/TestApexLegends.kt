package ren.natsuyuk1.comet.test.network.thirdparty.apex

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.ApexLegendAPI.fetchUserID
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestApexLegends {
    @BeforeAll
    fun init() {
        if (isCI()) return

        runBlocking {
            CometConfig.init()
        }
    }

    @Test
    fun testUsernameToUID() {
        if (isCI()) return

        val username = "NaTsuYuk1_"

        runBlocking {
            cometClient.fetchUserID(username, "PC").print()
        }
    }
}
