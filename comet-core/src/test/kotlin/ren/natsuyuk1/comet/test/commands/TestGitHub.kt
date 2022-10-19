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
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserTable
import ren.natsuyuk1.comet.commands.GithubCommand
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestGitHub {
    @BeforeAll
    fun init() {
        runBlocking {
            initTestDatabase()
            DatabaseManager.loadTables(UserTable)
            GitHubRepoData.init()
        }
    }

    @Test
    fun testSetting() {
        val fakeUser = CometUser.create(1L, LoginPlatform.TEST)
        GitHubRepoData.data.repos.add(
            GitHubRepoData.Data.GithubRepo(
                "Comet-Bot",
                "StarWishsama",
                "",
                mutableListOf()
            )
        )
        runBlocking {
            GithubCommand(
                fakeComet,
                generateFakeSender(1),
                generateFakeGroup(1),
                buildMessageWrapper {
                    appendText("setting StarWishsama/Comet-Bot add StarWishsama/Comet-Bot -e pull_request")
                },
                fakeUser
            ).main("setting StarWishsama/Comet-Bot add StarWishsama/Comet-Bot -e pull_request").print()
        }
    }

    @AfterAll
    fun cleanup() {
        transaction {
            UserTable.deleteAll()
        }
    }
}
