package ren.natsuyuk1.comet.network.thirdparty.jikipedia

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.utils.json.serializeTo

private val logger = mu.KotlinLogging.logger {}

object JikiPediaAPI {
    suspend fun search(keyword: String): JikiPediaSearchResult {
        return cometClient.client.post("https://api.jikipedia.com/go/search_entities") {
            headers {
                append("Origin", "https://jikipedia.com")
                append("Referer", "https://jikipedia.com")
                /* ktlint-disable max-line-length */
                append(
                    "xid",
                    "pZBzqk4B5uHQDyU2satS+FKft78gvi+PruIpjhHJdfudi4PAcYs/TdhfQQeYZxvF8WR0KZM4FHUxK3dPm7rLfC3hexA1MFvsSw3R/eVPw48="
                )
                /* ktlint-enable max-line-length */
                append("User-Agent", CometConfig.data.useragent)
                append("Client", "web")
                append("Client-Version", "2.7.3a")
                append("Content-Type", "application/json;charset=UTF-8")
                append("Accept", "application/json, text/plain, */*")
            }

            setBody(json.encodeToString(JikiPediaSearchRequest(keyword)))
        }.also { logger.debug { it } }.bodyAsText().serializeTo(json)
    }
}
