/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.CometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiEventList
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiUserInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.SekaiProfileEventInfo
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.sekaibest.SekaiBestPredictionInfo
import java.io.InputStream

private val logger = mu.KotlinLogging.logger {}

object ProjectSekaiAPI {
    /**
     * Project Sekai Profile Route
     *
     * 注意: %user_id% 就是原本的请求 URL, 不是参数
     */
    private const val PROFILE_URL = "https://api.pjsekai.moe"

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

    suspend fun CometClient.getUserEventInfo(eventID: Int, userID: Long): SekaiProfileEventInfo {
        logger.debug { "Fetching project sekai event $eventID rank for user $userID" }

        val resp = client.get<HttpStatement>("$PROFILE_URL/api/user/%7Buser_id%7D/event/$eventID/ranking") {
            url {
                parameters.append("targetUserId", userID.toString())
            }
        }.execute().receive<InputStream>()

        return resp.bufferedReader()
            .use { json.decodeFromString(it.readText().also { logger.debug { "Raw content: $it" } }) }
    }

    suspend fun CometClient.getSpecificRankInfo(eventID: Int, rankPosition: Int): SekaiProfileEventInfo {
        logger.debug { "Fetching project sekai event $eventID rank position at $rankPosition" }

        val resp = client.get<HttpStatement>("$PROFILE_URL/api/user/%7Buser_id%7D/event/$eventID/ranking") {
            url {
                parameters.append("targetRank", rankPosition.toString())
            }
        }.execute().receive<InputStream>()

        return resp.bufferedReader()
            .use { json.decodeFromString(it.readText().also { logger.debug { "Raw content: $it" } }) }
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
        }
    }

    suspend fun CometClient.getRankPredictionInfo(): SekaiBestPredictionInfo {
        logger.debug { "Fetching project sekai rank prediction info" }

        return client.get(THREE3KIT_URL)
    }

    suspend fun CometClient.getUserInfo(id: Long): ProjectSekaiUserInfo {
        logger.debug { "Fetching project sekai user info for $id" }

        val resp = client.get<HttpStatement>("$PROFILE_URL/api/user/$id/profile").execute().receive<InputStream>()

        return resp.bufferedReader()
            .use { json.decodeFromString(it.readText().also { logger.debug { "Raw content: $it" } }) }
    }
}
