package ren.natsuyuk1.comet.test.commands.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestGitHubService {

    @Test
    fun testRepoRegex() {
        val repoRegex = """(\w*)/(.*)""".toRegex()

        assertTrue { "StarWishsama/Slimefun4".matches(repoRegex) }

        val githubLinkRegex by lazy { Regex("""(https?://)?(www\.)?github\.com/(\w+)/(.+)""") }

        assertTrue { "https://github.com/StarWishsama/Comet-Bot".matches(githubLinkRegex) }
    }
}
