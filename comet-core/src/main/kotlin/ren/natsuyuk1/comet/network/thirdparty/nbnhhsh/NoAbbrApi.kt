package ren.natsuyuk1.comet.network.thirdparty.nbnhhsh

import io.ktor.client.request.*
import io.ktor.http.*
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.nbnhhsh.data.NoAbbrSearchRequest
import ren.natsuyuk1.comet.network.thirdparty.nbnhhsh.data.NoAbbrSearchResult

object NoAbbrApi {
    private const val apiRoute = "https://lab.magiconch.com/api/nbnhhsh/guess"

    suspend fun search(keyword: String): List<NoAbbrSearchResult> =
        cometClient.client.post(apiRoute) {
            body = NoAbbrSearchRequest(keyword)
            contentType(ContentType.parse("application/json"))
        }
}
