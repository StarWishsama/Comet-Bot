package ren.natsuyuk1.comet.test.network.thirdparty.saucenao

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.network.thirdparty.saucenao.SauceNaoApi
import ren.natsuyuk1.comet.network.thirdparty.saucenao.data.toMessageWrapper
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSauceNaoAPI {
    @BeforeAll
    fun init() {
        if (isCI()) return

        runBlocking {
            CometGlobalConfig.init()
        }
    }

    @Test
    fun test() {
        if (isCI()) return

        runBlocking {
            val fakeImage = Image(url = "https://i.pximg.net/img-master/img/2022/09/05/00/00/21/101003832_p0_master1200.jpg")
            SauceNaoApi.searchByImage(fakeImage).toMessageWrapper().print()
        }
    }
}
