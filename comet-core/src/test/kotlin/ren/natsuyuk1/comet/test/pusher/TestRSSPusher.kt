package ren.natsuyuk1.comet.test.pusher

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.comet.pusher.CometPushTargetType
import ren.natsuyuk1.comet.pusher.CometPusherContextTable
import ren.natsuyuk1.comet.pusher.impl.rss.RSSPusher
import ren.natsuyuk1.comet.test.fakeGroups
import ren.natsuyuk1.comet.test.generateFakeGroup
import ren.natsuyuk1.comet.test.initTestDatabase
import ren.natsuyuk1.comet.test.isCI
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestRSSPusher {
    @BeforeAll
    fun init() {
        if (isCI()) return

        initTestDatabase()
        DatabaseManager.loadTables(CometPusherContextTable)
        RSSPusher.init()
        RSSPusher.subscriber["https://rsshub.app/36kr/newsflashes"] =
            mutableListOf(CometPushTarget(id = 1, CometPushTargetType.GROUP, CometPlatform.TEST))
    }

    @Test
    fun test() {
        if (isCI()) return

        generateFakeGroup(1).also { fakeGroups.add(it) }

        runBlocking {
            RSSPusher.retrieve()
            RSSPusher.push()
        }
    }

    @AfterAll
    fun cleanup() {
        if (isCI()) return

        runBlocking {
            RSSPusher.stop()
        }

        transaction {
            CometPusherContextTable.deleteAll()
        }

        DatabaseManager.close()
    }
}
