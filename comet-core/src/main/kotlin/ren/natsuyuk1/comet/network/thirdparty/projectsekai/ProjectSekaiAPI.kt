/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.decodeFromString
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.CometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiEventList
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiRankSeasonInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiProfileEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.kit33.PJSKCheerfulPreditionInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.kit33.PJSKEventPredictionInfo
import ren.natsuyuk1.comet.utils.json.serializeTo

private val logger = mu.KotlinLogging.logger {}

object ProjectSekaiAPI {
    /**
     * Project Sekai Profile Route
     *
     * 注意: %user_id% 就是原本的请求 URL, 不是参数
     */
    private const val PROFILE_URL = "https://api.pjsekai.moe"

    /**
     * Unibot API
     * 似乎是上方的自部署版本 (?)
     */
    private const val UNIBOT_API_URL = "https://api.unipjsk.com"

    /**
     * pjsek.ai Route
     *
     * 同样有很奇怪的请求参数, 请多加留意
     */
    private const val PJSEKAI_URL = "https://api.pjsek.ai/database/master"

    /**
     * 33 Kit
     * https://3-3.dev/
     */
    private const val THREE3KIT_URL = "https://33.dsml.hk/pred"

    private suspend fun CometClient.profileRequest(
        param: String,
        builder: URLBuilder.(URLBuilder) -> Unit = {},
    ): HttpResponse {
        return coroutineScope {
            val unibotReq = async {
                client.get("$UNIBOT_API_URL$param") {
                    url(builder)
                }.let {
                    if (!it.status.isSuccess()) null else it
                }
            }

            val profileReq = async {
                client.get("$PROFILE_URL$param") {
                    url(builder)
                }.let {
                    if (!it.status.isSuccess()) null else it
                }
            }

            return@coroutineScope profileReq.await() ?: unibotReq.await() ?: error("Network error")
        }
    }

    suspend fun CometClient.getUserEventInfo(eventID: Int, userID: Long): SekaiProfileEventInfo {
        logger.debug { "Fetching project sekai event $eventID rank for user $userID" }

        val resp = profileRequest("/api/user/%7Buser_id%7D/event/$eventID/ranking") {
            parameters.append("targetUserId", userID.toString())
        }

        if (resp.status != HttpStatusCode.OK) {
            error("API return code isn't OK (${resp.status}), raw request url: ${resp.call.request.url}")
        }

        return json.decodeFromString(resp.bodyAsText().also { logger.debug { "Raw content: $it" } })
    }

    suspend fun CometClient.getSpecificRankInfo(eventID: Int, rankPosition: Int): SekaiProfileEventInfo {
        logger.debug { "Fetching project sekai event $eventID rank position at $rankPosition" }

        val resp = profileRequest("/api/user/%7Buser_id%7D/event/$eventID/ranking") {
            parameters.append("targetRank", rankPosition.toString())
        }

        if (resp.status != HttpStatusCode.OK) {
            error("API return code isn't OK (${resp.status}), raw request url: ${resp.call.request.url}")
        }

        return json.decodeFromString(resp.bodyAsText().also { logger.debug { "Raw content: $it" } })
    }

    suspend fun CometClient.getEventList(limit: Int = 12, startAt: Int = -1, skip: Int = 0): ProjectSekaiEventList {
        logger.debug { "Fetching project sekai event list" }

        return client.get("$PJSEKAI_URL/events") {
            url {
                parameters.apply {
                    append("$" + "limit", limit.toString())
                    append("$" + "sort[startAt]", startAt.toString())
                    append("$" + "skip", skip.toString())
                }
            }
        }.bodyAsText().serializeTo(json)
    }

    suspend fun CometClient.getRankPredictionInfo(): PJSKEventPredictionInfo {
        logger.debug { "Fetching project sekai rank prediction info" }

        return client.get(THREE3KIT_URL).bodyAsText().serializeTo(json)
    }

    suspend fun CometClient.getUserInfo(id: Long): ProjectSekaiUserInfo {
        logger.debug { "Fetching project sekai user info for $id" }

        val resp = profileRequest("/api/user/$id/profile")

        if (resp.status != HttpStatusCode.OK) {
            error("API return code isn't OK (${resp.status}), raw request url: ${resp.call.request.url}")
        }

        return json.decodeFromString(resp.bodyAsText().also { logger.debug { "Raw content: $it" } })
    }

    suspend fun CometClient.getRankSeasonInfo(userId: Long, rankSeasonId: Int): ProjectSekaiRankSeasonInfo {
        logger.debug { "Fetching project sekai rank season #$rankSeasonId info for $userId" }

        val resp = profileRequest("/api/user/%7Buser_id%7D/rank-match-season/$rankSeasonId/ranking") {
            parameters.append("targetUserId", userId.toString())
        }

        if (resp.status != HttpStatusCode.OK) {
            error("API return code isn't OK (${resp.status}), raw request url: ${resp.call.request.url}")
        }

        return json.decodeFromString(resp.bodyAsText().also { logger.debug { "Raw content: $it" } })
    }

    suspend fun CometClient.getCheerPredictData(): PJSKCheerfulPreditionInfo {
        logger.debug { "Fetching project sekai cheerful event predict info" }

        val resp = client.get("https://33.dsml.hk/cheer-pred")

        if (resp.status != HttpStatusCode.OK) {
            error("API return code isn't OK (${resp.status}), raw request url: ${resp.call.request.url}")
        }

        return json.decodeFromString(resp.bodyAsText().also { logger.debug { "Raw content: $it" } })
    }
}
