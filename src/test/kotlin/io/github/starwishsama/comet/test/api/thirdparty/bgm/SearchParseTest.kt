package io.github.starwishsama.comet.test.api.thirdparty.bgm

import io.github.starwishsama.comet.api.thirdparty.bgm.data.common.SearchType
import io.github.starwishsama.comet.api.thirdparty.bgm.parser.SubjectSearchResults
import io.github.starwishsama.comet.test.util.alsoPrint
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchParseTest {
    private val type = SearchType.Subject.All

    private val keyword = "日常"

    private lateinit var results: SubjectSearchResults

    @BeforeAll
    fun init(): Unit = runBlocking {
        results = bgmCrawler.searchSubject(type, keyword)
    }

    @Test
    fun `parse items`() = runBlocking {
        assert(results.items.alsoPrint().isNotEmpty())
    }

}
