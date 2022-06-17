/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.decodeFromString
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.CometClient
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiEventList
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiProfile

private val logger = mu.KotlinLogging.logger {}

object ProjectSekaiAPI {
    /**
     * Project Sekai Profile Route
     *
     * 注意: %user_id% 就是原本的请求 URL, 不是参数, 非常优秀的设计
     */
    private const val PROFILE_URL = "https://api.pjsekai.moe/api/user/%7Buser_id%7D/"

    /**
     * pjsek.ai Route
     *
     * 同样有很奇怪的请求参数, 请多加留意
     */
    private const val PJSEKAI_URL = "https://api.pjsek.ai/database/master"

    suspend fun CometClient.getUserEventInfo(eventID: Int, userID: ULong): ProjectSekaiProfile {
        logger.debug { "Fetching project sekai event $eventID rank for user $userID" }

        val resp = client.get("$PROFILE_URL/event/$eventID/ranking") {
            url {
                parameters.append("targetUserId", userID.toString())
            }
        }

        return resp.bodyAsChannel().toInputStream().bufferedReader().use { json.decodeFromString(it.readText()) }
    }

    suspend fun CometClient.getSpecificRankInfo(eventID: Int, rankPosition: ULong): ProjectSekaiProfile {
        logger.debug { "Fetching project sekai event $eventID rank position at $rankPosition" }

        val resp = client.get("$PROFILE_URL/event/$eventID/ranking") {
            url {
                parameters.append("targetRank", rankPosition.toString())
            }
        }

        return resp.bodyAsChannel().toInputStream().bufferedReader().use { json.decodeFromString(it.readText()) }
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
        }.body()
    }
}
