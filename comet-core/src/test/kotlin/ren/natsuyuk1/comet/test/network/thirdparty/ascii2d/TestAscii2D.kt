package ren.natsuyuk1.comet.test.network.thirdparty.ascii2d

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.message.asURLImage
import ren.natsuyuk1.comet.network.thirdparty.ascii2d.Ascii2DApi
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestAscii2D {
    @Test
    fun test() {
        if (isCI()) return

        runBlocking {
            Ascii2DApi.searchByImage(
                "https://i.pximg.net/img-master/img/2022/04/16/15/45/06/97661462_p0_master1200.jpg".asURLImage(),
            ) // ktlint-disable max-line-length
                .print()
        }
    }
}
