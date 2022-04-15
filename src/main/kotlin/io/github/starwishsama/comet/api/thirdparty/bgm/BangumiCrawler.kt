package io.github.starwishsama.comet.api.thirdparty.bgm

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.compression.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.starwishsama.comet.api.thirdparty.bgm.const.MAIN_DOMAIN
import io.github.starwishsama.comet.api.thirdparty.bgm.const.buildSubjectUrl
import io.github.starwishsama.comet.api.thirdparty.bgm.data.common.SearchType
import io.github.starwishsama.comet.api.thirdparty.bgm.parser.Subject
import io.github.starwishsama.comet.api.thirdparty.bgm.parser.SubjectSearchResults

internal val bgmCrawler by lazy {
    BangumiCrawler()
}

@JvmInline
value class BangumiCrawler(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentEncoding) {
            gzip()
            deflate()
            identity()
        }
        install(UserAgent) {
            agent = Crawler.userAgents.random()
        }
    }
) {
    suspend fun fetchSubject(url: String) = withContext(Dispatchers.IO) {
        Subject(client.get(url))
    }

    suspend fun fetchSubject(id: Long) = withContext(Dispatchers.IO) {
        fetchSubject(buildSubjectUrl(id))
    }

    private suspend fun rawSearch(type: SearchType, keyword: String, exactMode: Boolean = false): String {
        val path = when (type) {
            is SearchType.Subject -> "/subject_search/"
            is SearchType.Person -> "/mono_search/"
        }
        val url = MAIN_DOMAIN + path + keyword.encodeURLPath()
        return client.get(url) {
            parameter("cat", type.category)
            if (exactMode) parameter("legacy", "1")
        }
    }

    suspend fun searchSubject(
        type: SearchType.Subject,
        keyword: String,
        exactMode: Boolean = false
    ): SubjectSearchResults = SubjectSearchResults(rawSearch(type, keyword, exactMode))
}
