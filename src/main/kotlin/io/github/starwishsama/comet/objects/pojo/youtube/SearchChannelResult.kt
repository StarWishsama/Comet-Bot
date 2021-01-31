package io.github.starwishsama.comet.objects.pojo.youtube

import com.google.gson.JsonObject

data class SearchChannelResult(
    val kind: String = "youtube#channelListResponse",
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
            val thumbnails: JsonObject
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
"kind": "youtube#channelListResponse",
"etag": "gSlmHO988MiNC1yWWmFzrAVlnd0",
"pageInfo": {
"totalResults": 1,
"resultsPerPage": 5
},
"items": [
{
"kind": "youtube#channel",
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
