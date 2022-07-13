package ren.natsuyuk1.comet.test.commands.service

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
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

private val logger = mu.KotlinLogging.logger {}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSignService {
    @BeforeAll
    fun init() {
        initTestDatabase()
        DatabaseManager.loadTables(UserTable, UserPermissionTable)
    }

    @Test
    fun testCheckInPosition() {
        val testCheckInTime = Clock.System.now()

        val testUser = CometUser.create(114514L).also {
            transaction {
                it.checkInDate = testCheckInTime.plus(1.minutes).toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
        val testUser2 = CometUser.create(114515L).also {
            transaction {
                it.checkInDate = testCheckInTime.toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
        val testUser3 = CometUser.create(114516L).also {
            transaction {
                it.checkInDate = testCheckInTime.plus(2.minutes).toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }

        assertEquals(1, SignInService.getSignInPosition(testUser))

        transaction {
            UserTable.deleteWhere {
                UserTable.id eq testUser.id
                UserTable.id eq testUser2.id
                UserTable.id eq testUser3.id
            }
        }
    }
}
