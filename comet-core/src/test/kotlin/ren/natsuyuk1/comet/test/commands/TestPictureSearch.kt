package ren.natsuyuk1.comet.test.commands

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.PictureSearchCommand
import ren.natsuyuk1.comet.objects.command.picturesearch.PictureSearchConfigTable
import ren.natsuyuk1.comet.test.fakeComet
import ren.natsuyuk1.comet.test.generateFakeSender
import ren.natsuyuk1.comet.test.initTestDatabase
import kotlin.test.Test
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestPictureSearch {
    @BeforeAll
    fun init() {
        runBlocking {
            CometGlobalConfig.init()
        }

        initTestDatabase()
        DatabaseManager.loadTables(PictureSearchConfigTable, UserTable)
    }

    @Test
    fun test() {
        val fakeSender = generateFakeSender(1)
        val testMsg = buildMessageWrapper {
            appendText("/ps")
        }

        val fakeUser = CometUser.create(1L, LoginPlatform.TEST)

        val fakeImage = buildMessageWrapper {
            // https://www.pixiv.net/artworks/92690006
            appendElement(Image("https://i.pximg.net/img-master/img/2021/09/12/13/55/20/92690006_p0_master1200.jpg"))
        }

        runBlocking {
            PictureSearchCommand(
                fakeComet,
                fakeSender,
                fakeSender,
                testMsg,
                fakeUser
            ).main("")
        }

        val result = SessionManager.handleSession(fakeSender, fakeSender, fakeImage)

        assertTrue(result, "Picture search request wasn't be handled!")
    }

    @AfterAll
    fun cleanup() {
        transaction {
            UserTable.deleteAll()
        }
    }
}
