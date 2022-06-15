/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.network.thirdparty.projectsekai

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.decodeFromString
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.ProjectSekaiProfile

private val logger = mu.KotlinLogging.logger {}

object ProjectSekaiAPI {
    /**
     * Project Sekai Profile Route
     *
     * 注意: %user_id% 就是原本的请求 URL, 不是参数
     */
    private const val UPSTREAM = "https://api.pjsekai.moe/api/user/%7Buser_id%7D/"

    suspend fun HttpClient.getUserEventInfo(eventID: Int, userID: Long): ProjectSekaiProfile {
        val resp = get("$UPSTREAM/event/$eventID/ranking") {
            url {
                parameters.append("targetUserId", userID.toString())
            }
        }.bodyAsChannel()

        return resp.toInputStream().bufferedReader().use { json.decodeFromString(it.readText()) }
    }
}
