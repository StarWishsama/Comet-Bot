package ren.natsuyuk1.comet.network.thirdparty.nbnhhsh

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.nbnhhsh.data.NoAbbrSearchRequest
import ren.natsuyuk1.comet.network.thirdparty.nbnhhsh.data.NoAbbrSearchResult
import ren.natsuyuk1.comet.utils.json.serializeTo

object NoAbbrApi {
    private const val apiRoute = "https://lab.magiconch.com/api/nbnhhsh/guess"

    suspend fun search(keyword: String): List<NoAbbrSearchResult> =
        cometClient.client.post(apiRoute) {
            setBody(json.encodeToString(NoAbbrSearchRequest(keyword)))
            contentType(ContentType.parse("application/json"))
        }.bodyAsText().serializeTo(json)
}
