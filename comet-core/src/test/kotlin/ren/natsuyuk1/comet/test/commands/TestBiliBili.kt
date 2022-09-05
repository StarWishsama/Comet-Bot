package ren.natsuyuk1.comet.test.commands

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.service.BiliBiliService
import ren.natsuyuk1.comet.network.thirdparty.bilibili.initYabapi
import ren.natsuyuk1.comet.test.generateFakeGroup
import ren.natsuyuk1.comet.test.generateFakeGroupMember
import ren.natsuyuk1.comet.test.isCI
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestBiliBili {
    @BeforeAll
    fun init() {
        if (isCI()) return

        DatabaseManager.loadDatabase()
        DatabaseManager.loadTables(UserTable)
        runBlocking {
            initYabapi()
        }
    }

    @Test
    fun testSearchUser() {
        if (isCI()) return

        val group = generateFakeGroup(114514)
        val dummy1 = generateFakeGroupMember(1, group)
        val dummy2 = generateFakeGroupMember(2, group)

        transaction {
            CometUser.create(dummy1.id, dummy1.platform)
            CometUser.create(dummy2.id, dummy2.platform)
        }

        runBlocking {
            BiliBiliService.processUserSearch(group, dummy1, keyword = "Elysian绿豆").join()
        }

        assertTrue("BiliBili Session isn't be registered properly!") { SessionManager.getSessionCount() == 1 }

        val handleResult = SessionManager.handleSession(group, dummy2, buildMessageWrapper { appendText("1") })

        assertFalse("BiliBili session be processed wrongly! 2 ($handleResult)") { handleResult }

        val handleResult2 = SessionManager.handleSession(group, dummy1, buildMessageWrapper { appendText("1") })

        assertTrue("BiliBili session be processed wrongly! 1 ($handleResult)") { handleResult2 }
    }

    @AfterAll
    fun cleanup() {
        if (isCI()) return

        transaction {
            UserTable.deleteAll()
        }
    }
}
