/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.noabbr

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.api.thirdparty.noabbr.data.AbbrSearchRequest
import io.github.starwishsama.comet.api.thirdparty.noabbr.data.AbbrSearchResponse
import io.github.starwishsama.comet.managers.NetworkRequestManager
import io.github.starwishsama.comet.utils.network.NetUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

object NoAbbrApi : ApiExecutor {
    private const val apiRouteURL = "https://lab.magiconch.com/api/nbnhhsh/guess"

    fun guessMeaning(abbr: String): AbbrSearchResponse {
        val call = NetUtil.executeRequest(
            apiRouteURL,
            method = "POST",
            body = mapper.writeValueAsString(AbbrSearchRequest(abbr)).trim()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )

        NetworkRequestManager.logRequest(apiRouteURL)

        val responseBody = call.execute().body ?: return AbbrSearchResponse.empty()

        val responseString = responseBody.string()

        NetworkRequestManager.finishRequest(apiRouteURL)

        val tree = mapper.readTree(responseString)

        if (tree.isNull || tree.isObject) {
            daemonLogger.warning("解析能不能好好说话回调失败: 回调结果异常 (${tree.nodeType})")
            return AbbrSearchResponse.empty()
        }

        if (tree.isEmpty) {
            return AbbrSearchResponse.empty()
        }

        val firstNode = tree[0]

        return if (firstNode.isObject) {
            mapper.readValue(firstNode.traverse())
        } else {
            daemonLogger.warning("解析能不能好好说话回调失败: 回调结果异常 ${firstNode.nodeType}")
            AbbrSearchResponse.empty()
        }
    }

    override var usedTime: Int = 0
    override val duration: Int = 1

    override fun getLimitTime(): Int = 60
}