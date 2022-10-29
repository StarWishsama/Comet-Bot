package ren.natsuyuk1.comet.test.network.thirdparty.ascii2d

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.ascii2d.Ascii2dApi
import ren.natsuyuk1.comet.network.thirdparty.ascii2d.toMessageWrapper
import ren.natsuyuk1.comet.test.isCI
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestAscii2d {
    @Test
    fun test() {
        if (isCI()) return
        runBlocking {
            val r =
                /* ktlint-disable */
                Ascii2dApi.searchImage("http://gchat.qpic.cn/gchatpic_new/2426600292/725656262-2843171224-BAF76ADF456D053234D17FAEBFFE9EBA/0?term=2&is_origin=0")
            /* ktlint-enable */
            println(r.toMessageWrapper())
            assertTrue(r.errorMessage.isBlank())
        }
    }
}
