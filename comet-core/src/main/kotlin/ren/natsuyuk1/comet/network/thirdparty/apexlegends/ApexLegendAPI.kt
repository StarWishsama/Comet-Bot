package ren.natsuyuk1.comet.network.thirdparty.apexlegends

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.CometClient
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.data.ApexIDInfo
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.data.ApexPlayerInfo
import ren.natsuyuk1.comet.utils.json.serializeTo

private val logger = KotlinLogging.logger {}

object ApexLegendAPI {
    private const val API_ROUTE = "https://api.mozambiquehe.re"

    suspend fun CometClient.fetchUserInfo(uid: String, platform: String): ApexPlayerInfo = client.get("$API_ROUTE/bridge") {
        parameter("auth", CometGlobalConfig.data.apexLegendToken)
        parameter("uid", uid)
        parameter("platform", platform)

        accept(ContentType.Any)
    }.also {
        logger.debug { "Request URL is ${it.request.url}" }
    }.bodyAsText().also {
        logger.debug { "Received body $it" }
    }.serializeTo(json)

    suspend fun CometClient.fetchUserInfoByName(playerName: String, platform: String): ApexPlayerInfo = client.get("$API_ROUTE/bridge") {
        parameter("auth", CometGlobalConfig.data.apexLegendToken)
        parameter("player", playerName)
        parameter("platform", platform)

        accept(ContentType.Any)
    }.also {
        logger.debug { "Request URL is ${it.request.url}" }
    }.bodyAsText().also {
        logger.debug { "Received body $it" }
    }.serializeTo(json)

    suspend fun CometClient.fetchUserID(playerName: String, platform: String): ApexIDInfo = client.get("$API_ROUTE/nametouid") {
        parameter("auth", CometGlobalConfig.data.apexLegendToken)
        parameter("player", playerName)
        parameter("platform", platform)

        accept(ContentType.Any)
    }.also {
        logger.debug { "Request URL is ${it.request.url}" }
    }.bodyAsText().also {
        logger.debug { "Received body $it" }
    }.serializeTo(json)
}
