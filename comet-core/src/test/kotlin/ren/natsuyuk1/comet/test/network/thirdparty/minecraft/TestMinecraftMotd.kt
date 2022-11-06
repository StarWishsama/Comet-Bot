package ren.natsuyuk1.comet.test.network.thirdparty.minecraft

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ren.natsuyuk1.comet.network.thirdparty.minecraft.MinecraftServerType
import ren.natsuyuk1.comet.network.thirdparty.minecraft.query
import ren.natsuyuk1.comet.test.isCI

class TestMinecraftMotd {
    @Test
    fun test() {
        if (isCI()) return

        runBlocking {
            val r = query("127.0.0.1", 25565, MinecraftServerType.JAVA)
            assert(r != null)
            println(r!!.toMessageWrapper())
        }
    }
}
