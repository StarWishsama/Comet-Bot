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
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.CommonResponse

data class SearchUserResult(
    @JsonProperty("data")
    var data: Data,
) : CommonResponse() {
    data class Data(
        @JsonProperty("items")
        var items: List<Item>,
        @JsonProperty("pages")
        var pages: Int, // 2
        @JsonProperty("total")
        var total: Int, // 0
        @JsonProperty("trackid")
        var trackid: String // 15623048138266462990
    ) {
        data class Item(
            @JsonProperty("archives")
            var archives: Int, // 1
            @JsonProperty("av_items")
            var avItems: List<AvItem>,
            @JsonProperty("cover")
            var cover: String, // https://i0.hdslb.com/bfs/face/c3200c52ae76004fbbab44010990431d0604aee5.jpg
            @JsonProperty("fans")
            var fans: Int, // 3
            @JsonProperty("goto")
            var goto: String, // author
            @JsonProperty("is_up")
            var isUp: Boolean, // true
            @JsonProperty("level")
            var level: Int, // 3
            @JsonProperty("live_status")
            var liveStatus: Int, // 1
            @JsonProperty("live_uri")
            var liveUri: String, // bilibili://live/3234638?broadcast_type=0
            @JsonProperty("mid")
            var mid: Long, // 32557668
            @JsonProperty("official_verify")
            var officialVerify: OfficialVerify,
            @JsonProperty("param")
            var `param`: String, // 32557668
            @JsonProperty("roomid")
            var roomid: Long, // 3234638
            @JsonProperty("sign")
            var sign: String, // 担心额刚好阿西
            @JsonProperty("title")
            var title: String, // 刀剑神域小漠
            @JsonProperty("uri")
            var uri: String // bilibili://author/32557668
        ) {
            data class OfficialVerify(
                @JsonProperty("type")
                var type: Int // 127
            )

            data class AvItem(
                @JsonProperty("cover")
                var cover: String, // https://i0.hdslb.com/bfs/archive/95be7c1a940dda2bbf4c33213df94eb650e44d10.jpg
                @JsonProperty("ctime")
                var ctime: Int, // 1535755416
                @JsonProperty("danmaku")
                var danmaku: Int, // 1
                @JsonProperty("duration")
                var duration: String, // 3:1
                @JsonProperty("goto")
                var goto: String, // av
                @JsonProperty("param")
                var `param`: String, // 30843572
                @JsonProperty("play")
                var play: Int, // 15
                @JsonProperty("title")
                var title: String, // 官方认证：非洲正品大酋长
                @JsonProperty("uri")
                var uri: String // bilibili://video/30843572?player_width=1920&player_height=1080&player_rotate=0
            )
        }
    }
}