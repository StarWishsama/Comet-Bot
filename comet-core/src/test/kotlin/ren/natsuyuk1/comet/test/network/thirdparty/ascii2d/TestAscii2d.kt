package ren.natsuyuk1.comet.test.network.thirdparty.ascii2d

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.ascii2d.Ascii2dApi
import ren.natsuyuk1.comet.test.isCI
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestAscii2d {
    @Test
    fun test() {
        if (isCI()) return
        runBlocking {
            Ascii2dApi.searchImage("https://i.pximg.net/img-master/img/2022/10/16/16/46/17/101984525_p0_master1200.jpg")
        }
    }
}
