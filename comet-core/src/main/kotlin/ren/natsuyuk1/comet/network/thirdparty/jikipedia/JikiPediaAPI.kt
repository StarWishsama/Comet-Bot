package ren.natsuyuk1.comet.network.thirdparty.jikipedia

import io.ktor.client.request.*
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.consts.cometClient

private val logger = mu.KotlinLogging.logger {}

object JikiPediaAPI {
    suspend fun search(comet: Comet, keyword: String): JikiPediaSearchResult {
        return cometClient.client.post<JikiPediaSearchResult>("https://api.jikipedia.com/go/search_entities") {
            headers {
                append("Origin", "https://jikipedia.com")
                append("Referer", "https://jikipedia.com")
                append(
                    "xid",
                    "pZBzqk4B5uHQDyU2satS+FKft78gvi+PruIpjhHJdfudi4PAcYs/TdhfQQeYZxvF8WR0KZM4FHUxK3dPm7rLfC3hexA1MFvsSw3R/eVPw48="
                )
                append("User-Agent", CometGlobalConfig.data.useragent)
                append("Client", "web")
                append("Client-Version", "2.7.3a")
                append("Content-Type", "application/json;charset=UTF-8")
                append("Accept", "application/json, text/plain, */*")
            }

            body = JikiPediaSearchRequest(keyword)
        }.also { logger.debug { it } }
    }
}
