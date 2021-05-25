/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.youtube.data

data class YoutubeRequestError(
    val error: Error
) {
    data class Error(
        val code: Int,
        val message: String?,
        val errors: List<ErrorDetail>,
        val status: String?
    )

    data class ErrorDetail(
        val message: String,
        val domain: String,
        val reason: String?,
        val location: String?,
        val locationType: String?
    )
}