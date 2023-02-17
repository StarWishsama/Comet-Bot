/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.github.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoInfo(
    val id: Int,
    @SerialName("node_id")
    val nodeID: String,
    @SerialName("name")
    val repoName: String,
    @SerialName("full_name")
    val repoFullName: String,
    @SerialName("private")
    val private: Boolean,
    @SerialName("owner")
    val owner: OwnerInfo,
    @SerialName("html_url")
    val url: String,
    @SerialName("description")
    val description: String?,
) {
    @Serializable
    data class OwnerInfo(
        val login: String,
        val id: Int,
        @SerialName("html_url")
        val pageUrl: String,
    )
}
