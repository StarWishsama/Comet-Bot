package ren.natsuyuk1.comet.test.commands.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.service.ProjectSekaiManager
import ren.natsuyuk1.comet.test.isCI
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestProjectSekaiService {
    @BeforeAll
    fun initDatabase() {
        if (isCI()) return

        runBlocking {
            ProjectSekaiManager.init(EmptyCoroutineContext)
        }
    }

    @Test
    fun testMusicDiff() {
        if (isCI()) return

        // てらてら
        val musicId = 257
        val diff = 30 + 0.2789866632577603

        val fetched = ProjectSekaiManager.getSongAdjustedLevel(musicId, MusicDifficulty.MASTER)

        assertTrue("Wrong music diff (${fetched}), it should be $diff") { diff == fetched }
    }
}
