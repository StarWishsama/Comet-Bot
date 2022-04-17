package io.github.starwishsama.comet.test.api.thirdparty.bgm

import io.github.starwishsama.comet.api.thirdparty.bgm.parser.Subject
import io.github.starwishsama.comet.test.util.alsoPrint
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubjectParseTest {

    private val id: Long = 9912

    private lateinit var subject: Subject

    @BeforeAll
    private fun init(): Unit = runBlocking {
        subject = bgmCrawler.fetchSubject(id.alsoPrint())
    }

    @Test
    fun `parse id`(): Unit = runBlocking {
        assertEquals(id, subject.id.alsoPrint())
    }

    @Test
    fun `parse title`(): Unit = runBlocking {
        assertNotNull(id, subject.title.alsoPrint())
        assertNotNull(id, subject.translatedTitle.alsoPrint())
    }

    @Test
    fun `parse rank`(): Unit = runBlocking {
        assertNotNull(subject.rank.alsoPrint())
    }

    @Test
    fun `parse score`(): Unit = runBlocking {
        assertNotNull(subject.score.alsoPrint())
    }

    @Test
    fun `parse vote count`(): Unit = runBlocking {
        assertNotNull(subject.totalVote.alsoPrint())
    }

    @Test
    fun `parse vote chart`(): Unit = runBlocking {
        assertFalse { subject.voteChart.alsoPrint().isEmpty() }
        subject.voteChart.forEach {
            assertNotNull(it.value)
        }
    }

    @Test
    fun `parse summary`(): Unit = runBlocking {
        assertNotNull(subject.summary.alsoPrint())
    }

    @Test
    fun `parse tags`(): Unit = runBlocking {
        assertFalse { subject.tags.alsoPrint().isEmpty() }
    }

    @Test
    fun `parse ep`(): Unit = runBlocking {
        assertNotNull(subject.episodesUrl.alsoPrint())
    }

    @Test
    fun `parse simple episodes`(): Unit = runBlocking {
        assertFalse { subject.simpleEpisodes.alsoPrint().isEmpty() }
    }

    @Test
    fun `parse follow status`(): Unit = runBlocking {
        assertFalse { subject.followStatus.alsoPrint().isEmpty() }
    }
}
