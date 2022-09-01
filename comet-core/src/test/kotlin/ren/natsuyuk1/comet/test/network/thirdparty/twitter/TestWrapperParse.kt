package ren.natsuyuk1.comet.test.network.thirdparty.twitter

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.twitter.TwitterAPI
import ren.natsuyuk1.comet.network.thirdparty.twitter.initSetsuna
import ren.natsuyuk1.comet.network.thirdparty.twitter.toMessageWrapper
import ren.natsuyuk1.comet.objects.config.TwitterConfig
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.test.print
import kotlin.coroutines.EmptyCoroutineContext

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestWrapperParse {
    @BeforeAll
    fun init() {
        if (isCI()) return

        runBlocking {
            TwitterConfig.init()
            initSetsuna(EmptyCoroutineContext)
        }
    }

    @Test
    fun testWrapperParse() {
        if (isCI()) return

        runBlocking {
            val resp = TwitterAPI.fetchTweet("1563362417597648897")
            resp?.tweet?.toMessageWrapper(resp.includes)?.print()
        }
    }
}
