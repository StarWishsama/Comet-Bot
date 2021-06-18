/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.search

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse

/**
 * Web 端搜索结果
 *
 * 端点: http://api.bilibili.com/x/web-interface/search/type
 */
data class SearchUserResult(
    val data: Data
) : CommonResponse() {
    data class Data(
        val page: Int,
        @JsonProperty("pagesize")
        val pageSize: Int,
        @JsonProperty("rqt_type")
        val requestType: String,
        val result: SearchResults
    ) {
        data class SearchResult(
            val type: String,
            val mid: Long,
            @JsonProperty("uname")
            val userName: String,
            @JsonProperty("usign")
            val userSign: String,
            @JsonProperty("fans")
            val followers: Long,
            @JsonProperty("video")
            val videoCount: Long,
            @JsonProperty("upic")
            val userPicture: String,
            val level: Int,
            val gender: Int,
            @JsonProperty("is_live")
            val liveStatus: Int,
            @JsonProperty("room_id")
            val roomId: Long,
            @JsonProperty("official_verify")
            val verifyInfo: JsonNode,
            @JsonProperty("res")
            val recentVideos: List<RecentVideo>
        ) {
            data class RecentVideo(
                val aid: Long,
                @JsonProperty("bvid")
                val bid: String,
                val title: String,
                @JsonProperty("pubdate")
                val publishDate: Long,
                @JsonProperty("arcurl")
                val url: String,
                @JsonProperty("desc")
                val description: String,
                @JsonProperty("duration")
                val duration: String,
            )
        }
    }
}

typealias SearchResults = List<SearchUserResult.Data.SearchResult>
