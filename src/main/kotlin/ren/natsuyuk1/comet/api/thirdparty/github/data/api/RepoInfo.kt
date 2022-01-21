/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.github.data.api

import com.fasterxml.jackson.annotation.JsonProperty

data class RepoInfo(
    val id: Int,
    @JsonProperty("node_id")
    val nodeID: String,
    @JsonProperty("name")
    val repoName: String,
    @JsonProperty("full_name")
    val repoFullName: String,
    @JsonProperty("private")
    val private: Boolean,
    @JsonProperty("owner")
    val owner: OwnerInfo,
    @JsonProperty("html_url")
    val url: String,
    @JsonProperty("description")
    val description: String?
) {
    data class OwnerInfo(
        val login: String,
        val id: Int,
        @JsonProperty("html_url")
        val pageUrl: String
    )
}