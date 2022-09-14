package ren.natsuyuk1.comet.network.thirdparty.apexlegends

import io.ktor.client.call.*
import io.ktor.client.request.*
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.network.CometClient
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.data.ApexIDInfo
import ren.natsuyuk1.comet.network.thirdparty.apexlegends.data.ApexPlayerInfo

object ApexLegendAPI {
    private const val API_ROUTE = "https://api.mozambiquehe.re/"

    suspend fun CometClient.fetchUserInfo(uid: String, platform: String): ApexPlayerInfo = client.get("$API_ROUTE/bridge") {
        parameter("uid", uid)
        parameter("platform", platform)

        header("Authorization", CometGlobalConfig.data.apexLegendToken)
    }.body()

    suspend fun CometClient.fetchUserInfoByName(playerName: String, platform: String): ApexPlayerInfo = client.get("$API_ROUTE/bridge") {
        parameter("player", playerName)
        parameter("platform", platform)

        header("Authorization", CometGlobalConfig.data.apexLegendToken)
    }.body()

    suspend fun CometClient.fetchUserID(playerName: String, platform: String): ApexIDInfo = client.get("$API_ROUTE/nametouid") {
        parameter("player", playerName)
        parameter("platform", platform)

        header("Authorization", CometGlobalConfig.data.apexLegendToken)
    }.body()
}
