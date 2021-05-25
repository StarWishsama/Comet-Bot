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

import com.fasterxml.jackson.databind.JsonNode

data class SearchChannelResult(
    val kind: String = "data#channelListResponse",
    val etag: String,
    val pageInfo: SearchVideoResult.PageInfo,
    /**
     * 通过 ID 搜索时, 该链表长度只能 <= 1
     */
    val items: List<YoutubeChannel>
) {
    data class YoutubeChannel(
        val kind: String,
        val etag: String,
        val id: String,
        val snippet: Snippet,
        val statistics: Statistics
    ) {
        data class Snippet(
            val title: String,
            val description: String,
            val customUrl: String,
            val publishedAt: String,
            val thumbnails: JsonNode
        )

        data class Statistics(
            val viewCount: Long,
            val subscriberCount: Long,
            val hiddenSubscriberCount: Boolean,
            val videoCount: Long
        )
    }
}

/**
 * {
"kind": "data#channelListResponse",
"etag": "gSlmHO988MiNC1yWWmFzrAVlnd0",
"pageInfo": {
"totalResults": 1,
"resultsPerPage": 5
},
"items": [
{
"kind": "data#channel",
"etag": "9J7G7lJBbuw-RPiIEu0Fo75eLRA",
"id": "UC4YaOt1yT-ZeyB0OmxHgolA",
"snippet": {
"title": "A.I.Channel",
"description": "はじめまして！ みんなと、あなたと繋がりたい！キズナアイです(o・v・o)♪\nチャンネル登録よろしくお願いしますლ(´ڡ`ლ)",
"customUrl": "aichannel",
"publishedAt": "2016-10-19T06:03:24Z",
"thumbnails": {
"default": {
"url": "https://yt3.ggpht.com/ytc/AAUvwnhGnnDhdjO7gAkYmd5dvOdKQzgmU6lJfXZfC6CIoA=s88-c-k-c0x00ffffff-no-rj-mo",
"width": 88,
"height": 88
},
"medium": {
"url": "https://yt3.ggpht.com/ytc/AAUvwnhGnnDhdjO7gAkYmd5dvOdKQzgmU6lJfXZfC6CIoA=s240-c-k-c0x00ffffff-no-rj-mo",
"width": 240,
"height": 240
},
"high": {
"url": "https://yt3.ggpht.com/ytc/AAUvwnhGnnDhdjO7gAkYmd5dvOdKQzgmU6lJfXZfC6CIoA=s800-c-k-c0x00ffffff-no-rj-mo",
"width": 800,
"height": 800
}
},
"localized": {
"title": "A.I.Channel",
"description": "はじめまして！ みんなと、あなたと繋がりたい！キズナアイです(o・v・o)♪\nチャンネル登録よろしくお願いしますლ(´ڡ`ლ)"
},
"country": "JP"
},
"contentDetails": {
"relatedPlaylists": {
"likes": "",
"favorites": "",
"uploads": "UU4YaOt1yT-ZeyB0OmxHgolA"
}
},
"statistics": {
"viewCount": "359352019",
"subscriberCount": "2910000",
"hiddenSubscriberCount": false,
"videoCount": "985"
}
}
]
}

 */
