package io.github.starwishsama.comet.api.thirdparty.bgm

import io.github.starwishsama.comet.api.thirdparty.bgm.const.MAIN_DOMAIN
import io.github.starwishsama.comet.api.thirdparty.bgm.const.buildSubjectUrl
import io.github.starwishsama.comet.api.thirdparty.bgm.data.common.SearchType
import io.github.starwishsama.comet.api.thirdparty.bgm.parser.Subject
import io.github.starwishsama.comet.api.thirdparty.bgm.parser.SubjectSearchResults
import io.github.starwishsama.comet.utils.network.NetUtil
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

internal val bgmCrawler by lazy {
    BangumiCrawler()
}

class BangumiCrawler {
    suspend fun fetchSubject(url: String) = withContext(Dispatchers.IO) {
        Subject(NetUtil.getPageContent(url) ?: throw IOException("Unable to fetch bangumi page: $url"))
    }

    suspend fun fetchSubject(id: Long) = withContext(Dispatchers.IO) {
        fetchSubject(buildSubjectUrl(id))
    }

    private fun rawSearch(type: SearchType, keyword: String, exactMode: Boolean = false): String {
        val path = when (type) {
            is SearchType.Subject -> "/subject_search/"
            is SearchType.Person -> "/mono_search/"
        }
        val url = MAIN_DOMAIN + path + keyword.encodeURLPath()
        return NetUtil.getPageContent("$url?cat=${type.category}${if (exactMode) "&legacy=1" else ""}") ?: ""
    }

    fun searchSubject(
        type: SearchType.Subject,
        keyword: String,
        exactMode: Boolean = false
    ): SubjectSearchResults = SubjectSearchResults(rawSearch(type, keyword, exactMode))
}
