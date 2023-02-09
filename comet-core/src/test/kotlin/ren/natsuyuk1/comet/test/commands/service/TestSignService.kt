package ren.natsuyuk1.comet.test.commands.service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserPermissionTable
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.service.SignInService
import ren.natsuyuk1.comet.commands.service.isSigned
import ren.natsuyuk1.comet.test.initTestDatabase
import ren.natsuyuk1.comet.test.print
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
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

        val testUser = CometUser.create(114514L, CometPlatform.TEST).also {
            transaction {
                it.checkInDate = testCheckInTime.plus(1.minutes)
            }
        }
        CometUser.create(114515L, CometPlatform.TEST).also {
            transaction {
                it.checkInDate = testCheckInTime
            }
        }
        CometUser.create(114516L, CometPlatform.TEST).also {
            transaction {
                it.checkInDate = testCheckInTime.plus(2.minutes)
            }
        }

        assertEquals(1, SignInService.getSignInPosition(testUser))

        transaction {
            UserTable.deleteAll()
        }
    }

    @Test
    fun testIsSigned() {
        val testCheckInTime = Clock.System.now()

        val user = CometUser.getUserOrCreate(114514L, CometPlatform.MIRAI).also {
            transaction {
                it.checkInDate = testCheckInTime.minus(1.days)
            }
        }

        assertFalse { user.isSigned() }

        transaction {
            user.checkInDate = testCheckInTime.plus(1.days)
        }

        assertTrue { user.isSigned() }

        transaction {
            user.checkInDate = testCheckInTime
        }

        assertTrue { user.isSigned() }

        transaction {
            UserTable.deleteAll()
        }
    }

    @Test
    fun testLevelUp() {
        val user = CometUser.create(1, CometPlatform.TEST)
        runBlocking {
            transaction {
                user.exp = 1000
            }

            val upLevel = SignInService.levelUp(user.level, user.exp).also { it.print() }

            transaction {
                user.level += upLevel
            }
        }

        assertEquals(26, user.level)
    }

    @AfterAll
    fun cleanup() {
        transaction {
            UserTable.deleteAll()
        }
    }
}
