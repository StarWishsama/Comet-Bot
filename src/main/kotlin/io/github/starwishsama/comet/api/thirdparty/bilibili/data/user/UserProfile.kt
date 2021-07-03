/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.bilibili.data.user

import com.fasterxml.jackson.annotation.JsonProperty

data class UserProfile(
    val info: Info,
    val card: Card,
    @JsonProperty("vip")
    val vipInfo: VipInfo,
    val pendant: Pendant,
    @JsonProperty("decorate_card")
    val decorateCard: DecorateCard?,
    val rank: Int,
    val sign: String,
    @JsonProperty("level_info")
    val levelInfo: LevelInfo
) {
    data class DecorateCard(
        val mid: Long,
        val id: Int,
        @JsonProperty("card_url")
        val cardUrl: String,
        @JsonProperty("card_type")
        val cardType: Int,
        @JsonProperty("name")
        val cardName: String,
        /** 永久挂件为0 */
        @JsonProperty("expire_time")
        val expireTime: Long,
        /** 卡片分类，如大会员 */
        @JsonProperty("card_type_name")
        val cardTypeName: String,
        @JsonProperty("uid")
        val uid: Long,
        @JsonProperty("item_id")
        val itemId: Int,
        @JsonProperty("item_type")
        val itemType: Int,
        @JsonProperty("big_card_url")
        val bigCardUrl: String,
        @JsonProperty("jump_url")
        val jumpUrL: String,
        /** 粉丝挂件，如湊-阿库娅 */
        @JsonProperty("fan")
        val fan: Fan,
        @JsonProperty("image_enhance")
        val enhancedImage: String
    ) {
        data class Fan(
            @JsonProperty("is_fan")
            val isFan: Int,
            @JsonProperty("number")
            val number: Int,
            @JsonProperty("color")
            val color: String,
            @JsonProperty("num_desc")
            val numberDescription: String
        )
    }

    data class Info(
        var uid: Int = 0,
        @JsonProperty("uname")
        var userName: String?,
        @JsonProperty("face")
        var avatarImgURL: String?
    )

    data class Card(
        @JsonProperty("official_verify")
        val verifyInfo: VerifyInfo
    ) {
        data class VerifyInfo(
            val type: Int,
            val desc: String
        )
    }

    data class Pendant(
        val pid: Int,
        val name: String,
        val image: String,
        val expire: Int,
        @JsonProperty("image_enhance")
        val imageEnhance: String
    )

    data class LevelInfo(
        @JsonProperty("current_level")
        val currentLevel: Int,
        @JsonProperty("current_min")
        val currentMinLevel: Int,
        @JsonProperty("current_exp")
        val currentExp: Int,
        @JsonProperty("next_exp")
        val nextExp: Int
    )

    data class VipInfo(
        val vipType: Int,
        val vipDueDate: Long
    )
}

/**
 * "user_profile": {
"info": {
"uid": 1,
"uname": "bishi",
"face": "https://i0.hdslb.com/bfs/face/34c5b30a990c7ce4a809626d8153fa7895ec7b63.gif"
},
"card": {
"official_verify": {
"type": -1,
"desc": ""
}
},
"vip": {
"vipType": 2,
"vipDueDate": 1679932800000,
"dueRemark": "",
"accessStatus": 0,
"vipStatus": 1,
"vipStatusWarn": "",
"themeType": 0,
"label": {
"path": ""
}
},
"pendant": {
"pid": 0,
"name": "",
"image": "",
"expire": 0,
"image_enhance": ""
},
"rank": "10000",
"sign": "",
"level_info": {
"current_level": 4,
"current_min": 0,
"current_exp": 0,
"next_exp": "0"
}
}
 */