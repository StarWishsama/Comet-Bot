package ren.natsuyuk1.comet.network.thirdparty.saucenao

import io.ktor.client.request.*
import io.ktor.client.statement.*
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.saucenao.data.SauceNaoSearchResponse
import ren.natsuyuk1.comet.utils.json.serializeTo

private val logger = KotlinLogging.logger {}

object SauceNaoApi {
    private const val API_ROUTE = "https://saucenao.com/search.php"

    suspend fun searchByImage(img: Image): SauceNaoSearchResponse {
        require(img.url?.isNotBlank() == true) { "Image for saucenao search must not be empty!" }

        return cometClient.client.get(API_ROUTE) {
            parameter("db", "999")
            parameter("output_type", "2")
            parameter("url", img.url)
            // 2 = hide expected and suspected explicit
            parameter("hide", 2)
            parameter("api_key", CometGlobalConfig.data.sauceNaoToken)
        }.bodyAsText().apply {
            logger.debug { "Incoming saucenao response: $this" }
        }.serializeTo(json)
    }
}
