package ren.natsuyuk1.comet.test.commands

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.session.register
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.KeyWordAddSession
import ren.natsuyuk1.comet.test.generateFakeGroup
import ren.natsuyuk1.comet.test.generateFakeSender
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestKeyWord {
    @BeforeAll
    fun init() {
        DatabaseManager.loadDatabase()
        DatabaseManager.loadTables(UserTable)
    }

    @Test
    fun testKeyWordSession() {
        val sender = generateFakeSender(25)
        val group = generateFakeGroup(6823)
        val user = transaction {
            CometUser.create(sender.id, sender.platform)
        }

        KeyWordAddSession(sender, group, user, "TestKeyWord", false).register()

        assertTrue("KeyWordAddSession isn't be registered!") { SessionManager.getSessionCount() == 1 }

        val handleResult =
            runBlocking {
                SessionManager.handleSession(
                    group,
                    sender,
                    buildMessageWrapper { appendText("TestMessageWrapper") }
                )
            }

        assertTrue("KeyWordAddSession isn't be handled!") { handleResult }
    }

    @AfterAll
    fun cleanup() {
        transaction {
            UserTable.deleteAll()
        }

        DatabaseManager.close()
    }
}
