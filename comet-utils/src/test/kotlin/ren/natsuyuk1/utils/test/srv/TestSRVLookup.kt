package ren.natsuyuk1.utils.test.srv

import org.junit.jupiter.api.Test
import ren.natsuyuk1.comet.utils.srv.SRVLookup
import ren.natsuyuk1.utils.test.isCI
import kotlin.test.assertTrue

class TestSRVLookup {
    @Test
    fun test() {
        if (isCI()) return

        val testDomain = "hypixel.net"
        val serviceName = "minecraft"

        val result = SRVLookup.lookup(testDomain, serviceName)

        assertTrue { result?.first == "mc.hypixel.net" && result.second == 25565 }
    }
}
