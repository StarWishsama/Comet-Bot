package ren.natsuyuk1.comet.api.test.command

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.test.database.initTestDatabase
import ren.natsuyuk1.comet.api.test.fakeComet
import ren.natsuyuk1.comet.api.test.fakeSender
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCommandManager {
    @BeforeAll
    fun init() {
        initTestDatabase()
        DatabaseManager.loadTables(UserTable, UserPermissionTable)
        CommandManager.init()
        CommandManager.registerCommand(HELP) { comet, sender, subject, wrapper, user ->
            TestHelpCommand(
                comet,
                sender,
                subject,
                wrapper,
                user
            )
        }
    }

    @Test
    fun testCommandExecute() {
        runBlocking {
            CommandManager.executeCommand(
                fakeComet,
                fakeSender,
                fakeSender,
                buildMessageWrapper { appendText("/help") }
            ).join()
        }
    }
}
