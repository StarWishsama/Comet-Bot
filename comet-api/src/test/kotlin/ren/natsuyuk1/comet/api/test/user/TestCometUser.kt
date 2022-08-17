package ren.natsuyuk1.comet.api.test.user

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.test.database.initTestDatabase
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCometUser {
    @BeforeAll
    fun init() {
        initTestDatabase()
        DatabaseManager.loadTables(UserTable, UserPermissionTable)
    }

    @Test
    fun testUserCreate(): Unit = runBlocking {
        newSuspendedTransaction {
            UserTable.deleteWhere { UserTable.platformID eq 114514L }
            CometUser.create(114514L, LoginPlatform.TEST)
            assertTrue(CometUser.getUser(114514L, LoginPlatform.TEST) != null)
            UserTable.deleteWhere { UserTable.platformID eq 114514L }
        }
    }
}
