package ren.natsuyuk1.comet.test.commands.service

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.service.SignInService
import ren.natsuyuk1.comet.test.initTestDatabase
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSignService {
    @BeforeAll
    fun init() {
        initTestDatabase()
        DatabaseManager.loadTables(UserTable, UserPermissionTable)
    }

    @Test
    fun testSignInPosition() {
        val testCheckInTime = Clock.System.now()

        val testUser = CometUser.create(114514L).also {
            transaction {
                it.checkInDate = testCheckInTime.plus(1.minutes).toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
        CometUser.create(114515L).also {
            transaction {
                it.checkInDate = testCheckInTime.toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
        CometUser.create(114516L).also {
            transaction {
                it.checkInDate = testCheckInTime.plus(2.minutes).toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }

        assertEquals(1, SignInService.getSignInPosition(testUser))
    }

    @AfterAll
    fun cleanup() {
        transaction {
            UserTable.deleteWhere {
                UserTable.qq eq 114514L
                UserTable.qq eq 114515L
                UserTable.qq eq 114516L
            }
        }
    }
}
