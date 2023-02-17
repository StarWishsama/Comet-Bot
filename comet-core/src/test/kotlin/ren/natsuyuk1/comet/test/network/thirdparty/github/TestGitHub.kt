package ren.natsuyuk1.comet.test.network.thirdparty.github

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.github.GitHubApi
import ren.natsuyuk1.comet.test.isCI

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestGitHub {

    @Test
    fun testFileCommits() {
        if (isCI()) return

        runBlocking {
            println(
                GitHubApi.getSpecificFileCommits("Sekai-World", "sekai-i18n", "zh-TW/cheerful_carnival_teams.json")
                    .getOrThrow(),
            )
        }
    }
}
