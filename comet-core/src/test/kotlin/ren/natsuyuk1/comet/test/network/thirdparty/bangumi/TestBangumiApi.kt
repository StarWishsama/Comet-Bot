package ren.natsuyuk1.comet.test.network.thirdparty.bangumi

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.bangumi.BangumiOnlineApi
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestBangumiApi {
    @Test
    fun testBangumiScheduleFetch() {
        if (isCI()) return

        val data = runBlocking { BangumiOnlineApi.fetchBangumiSchedule() }
        data.getSpecificDaySchedule().print()
    }
}
