package ren.natsuyuk1.comet.api.test.session

import mu.KotlinLogging
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.database.AccountDataTable
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.session.register
import ren.natsuyuk1.comet.api.test.fakeComet
import ren.natsuyuk1.comet.api.test.fakeSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSessionManager {
    @BeforeAll
    fun init() {
        DatabaseManager.loadDatabase()
        DatabaseManager.loadTables(UserTable)
    }

    private class TestUser : User() {
        override val id: Long = 616
        override val remark: String = "TestUser"
        override val comet: Comet = fakeComet
        override val name: String = "TestUser"
        override var card: String = "TestUser"
        override val platform: LoginPlatform
            get() = LoginPlatform.TEST

        override fun sendMessage(message: MessageWrapper) {
            logger.debug { "Received message: $message" }
        }
    }

    @Test
    fun testSessionRegister() {
        val instance = transaction {
            CometUser.create(616, LoginPlatform.TEST)
        }

        val fakeUser = TestUser()

        TestSession(fakeSender, instance).register()

        assertTrue("Session List must have one session!") { SessionManager.getSessionCount() == 1 }

        assertTrue("User Session isn't be handle properly!") {
            SessionManager.handleSession(fakeUser, fakeUser, MessageWrapper().appendText("Test"))
        }
    }

    @AfterAll
    fun cleanup() {
        transaction {
            AccountDataTable.deleteAll()
        }
    }
}

internal class TestSession(contact: PlatformCommandSender, user: CometUser) : Session(contact, user) {
    override fun handle(message: MessageWrapper) {
        logger.debug { "Triggered test session!" }
    }
}
