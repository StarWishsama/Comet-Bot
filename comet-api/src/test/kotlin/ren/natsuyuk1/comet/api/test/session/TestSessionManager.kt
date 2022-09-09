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
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.session.Session
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.session.expire
import ren.natsuyuk1.comet.api.session.register
import ren.natsuyuk1.comet.api.test.fakeComet
import ren.natsuyuk1.comet.api.test.fakeSender
import ren.natsuyuk1.comet.api.user.*
import ren.natsuyuk1.comet.api.user.group.GroupPermission
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
        override val comet: Comet = fakeComet
        override val name: String = "TestUser"
        override val platform: LoginPlatform
            get() = LoginPlatform.TEST

        override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
            logger.debug { "Received message: $message" }
            return null
        }
    }

    private class TestGroup : Group(2434L, "NIJISANJI") {
        override val owner: GroupMember = TestGroupMember()

        override val members: List<GroupMember>
            get() = error("Unable to use this on test group")

        override fun updateGroupName(groupName: String) = error("Unable to use this on test group")

        override fun getBotMuteRemaining(): Int = error("Unable to use this on test group")

        override fun getBotPermission(): GroupPermission = error("Unable to use this on test group")

        override val avatarUrl: String
            get() = error("Unable to use this on test group")

        override fun getMember(id: Long): GroupMember = error("Unable to use this on test group")

        override suspend fun quit(): Boolean = error("Unable to use this on test group")

        override fun contains(id: Long): Boolean = error("Unable to use this on test group")

        override val comet: Comet = fakeComet
        override val platform: LoginPlatform = LoginPlatform.TEST

        override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
            logger.debug { "Group Received message: $message" }

            return null
        }
    }

    private class TestGroupMember : GroupMember() {
        override val group: Group
            get() = TestGroup()
        override val id: Long = 24341
        override val joinTimestamp: Int = 0
        override val lastActiveTimestamp: Int = 0
        override val remainMuteTime: Int = 0
        override val groupPermission: GroupPermission = GroupPermission.MEMBER

        override suspend fun mute(seconds: Int) = error("Unable to use this on test group member")

        override suspend fun unmute() = error("Unable to use this on test group member")

        override suspend fun kick(reason: String, block: Boolean) = error("Unable to use this on test group member")

        override suspend fun operateAdminPermission(operation: Boolean) =
            error("Unable to use this on test group member")

        override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
            logger.debug { "GroupMember received message: $message" }

            return null
        }

        override val comet: Comet = fakeComet
        override val name: String = "Mito"
        override var card: String = "Mito"
        override val platform: LoginPlatform = LoginPlatform.TEST
    }

    @Test
    fun testSessionRegister() {
        val fakeUser = TestUser()

        val instance = transaction {
            CometUser.create(fakeUser.id, LoginPlatform.TEST)
        }

        TestSession(fakeSender, instance).register()

        assertTrue("Session List must have one session!") { SessionManager.getSessionCount() == 1 }

        assertTrue("User Session isn't be handle properly!") {
            SessionManager.handleSession(fakeUser, fakeUser, MessageWrapper().appendText("Test"))
        }

        val fakeGroup = TestGroup()
        val fakeGroupSender = TestGroupMember()

        val instance2 = transaction {
            CometUser.create(fakeGroupSender.id, LoginPlatform.TEST)
        }

        TestSession(fakeGroupSender, instance2).register()

        assertTrue("Session List must have one session!") { SessionManager.getSessionCount() == 1 }

        assertTrue("User Session isn't be handle properly!") {
            SessionManager.handleSession(fakeGroup, fakeGroupSender, MessageWrapper().appendText("Test"))
        }
    }

    @AfterAll
    fun cleanup() {
        transaction {
            UserTable.deleteAll()
        }
    }
}

internal class TestSession(contact: PlatformCommandSender, user: CometUser) : Session(contact, user) {
    override suspend fun handle(message: MessageWrapper) {
        logger.debug { "Triggered test session!" }
        expire()
    }
}
