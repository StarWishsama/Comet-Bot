/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.github.data.api

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * [ErrorMessage]
 *
 * 调用 Github API 时发生异常时返回的 Json
 */
data class ErrorMessage(
    val message: String,
    @JsonProperty("documentation_url")
    val documentationUrl: String
) {
    private val notFound = "Not Found"

    fun isNotFound(): Boolean {
        return message == notFound
    }
}