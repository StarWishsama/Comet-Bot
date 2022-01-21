/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.bilibili.data.dynamic.dynamicdata

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.CometVariables.hmsPattern
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.Picture
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import java.time.LocalDateTime

data class Article(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("summary")
    val summary: String,
    @JsonProperty("author")
    val author: Author,
    @JsonProperty("image_urls")
    val imageURL: List<String>,
    @JsonProperty("publish_time")
    val publishTime: Long,
    @JsonProperty("stats")
    val stats: Stats,
    @JsonProperty("words")
    val wordLength: Long
) : DynamicData {

    data class Author(
        val mid: Long,
        val name: String,
        val face: String
    )

    data class Stats(
        val view: Long,
        val favorite: Long,
        val like: Long,
        val dislike: Long,
        val reply: Long,
        val share: Long,
        val coin: Long,
        val dynamic: Int
    )

    override fun asMessageWrapper(): MessageWrapper {
        val wrapped = MessageWrapper().addText(
            "${author.name} 发布了专栏 $title:\n" +
                    "$summary\n" +
                    "查看全文: https://www.bilibili.com/read/cv/$id\n" +
                    "\uD83D\uDC4D ${stats.like}|\uD83D\uDD01 ${stats.share}|🕘 ${hmsPattern.format(publishTime.toLocalDateTime())}"
        )

        if (imageURL.isNotEmpty()) {
            wrapped.addElement(Picture(imageURL[0]))
        }
        return wrapped
    }

    override fun getSentTime(): LocalDateTime = publishTime.toLocalDateTime()
}

/**
 * {
"id": 8335924,
"category": {
"id": 8,
"parent_id": 1,
"name": "手机游戏"
},
"categories": [{
"id": 1,
"parent_id": 0,
"name": "游戏"
}, {
"id": 8,
"parent_id": 1,
"name": "手机游戏"
}],
"title": "11月17日「团队战」开启预告",
"summary": "11月17日05:00起，游戏内将开启「团队战」！团队战中将会出现非常强力的怪物，与行会成员同心协力进行挑战吧！团队战结束后，将对团队战期间各行会对怪物造成的伤害总值进行排名。根据排名区间的不同，将对行会全员发放相应数量的报酬！一、团队战概要及作弊处理方式■活动时间11月17日05:00~11月22日23:59■团队战排名报酬预计发放时间11月26日 15:00开始陆续发放※由于报酬发放需要缓存，实际到账时间可能有一定延迟，还请骑士君们谅解。 ■本期团队战相对上一期团队战的变化本期行会排名报酬中",
"banner_url": "",
"template_id": 4,
"state": 0,
"author": {
"mid": 353840826,
"name": "公主连结ReDive",
"face": "https:\\/\\/i2.hdslb.com\\/bfs\\/face\\/4e8cba44ae5919aa72f99321edf5c0b0761eb270.jpg",
"pendant": {
"pid": 1887,
"name": "公主连结佩可莉姆",
"image": "https:\\/\\/i0.hdslb.com\\/bfs\\/garb\\/item\\/0688b6bcc9e5e96a19a2b91594774e71b0cf9d2a.png",
"expire": 0
},
"official_verify": {
"type": 1,
"desc": "公主连结ReDive手游官方账号"
},
"nameplate": {
"nid": 8,
"name": "知名偶像",
"image": "https:\\/\\/i0.hdslb.com\\/bfs\\/face\\/27a952195555e64508310e366b3e38bd4cd143fc.png",
"image_small": "https:\\/\\/i0.hdslb.com\\/bfs\\/face\\/0497be49e08357bf05bca56e33a0637a273a7610.png",
"level": "稀有勋章",
"condition": "所有自制视频总播放数>=100万"
},
"vip": {
"type": 2,
"status": 1,
"due_date": 0,
"vip_pay_type": 0,
"theme_type": 0,
"label": null
}
},
"reprint": 0,
"image_urls": ["https:\\/\\/i0.hdslb.com\\/bfs\\/article\\/df9363acdfac12bee542c67cee3a4bb0f20d307e.jpg"],
"publish_time": 1605240000,
"ctime": 1605179882,
"stats": {
"view": 33386,
"favorite": 27,
"like": 3881,
"dislike": 0,
"reply": 546,
"share": 91,
"coin": 12,
"dynamic": 0
},
"attributes": 24,
"words": 3055,
"origin_image_urls": ["https:\\/\\/i0.hdslb.com\\/bfs\\/article\\/1b256792945ffbf55d72b9f488e40dc3727ca786.jpg"],
"list": null,
"is_like": false,
"media": {
"score": 0,
"media_id": 0,
"title": "",
"cover": "",
"area": "",
"type_id": 0,
"type_name": "",
"spoiler": 0,
"season_id": 0
},
"apply_time": "",
"check_time": "",
"original": 0,
"act_id": 0,
"dispute": null,
"authenMark": null
}

 */