package io.github.starwishsama.comet.api.thirdparty.bilibili.data.user

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class UserProfile(
        val info: Info,
        val card: Card,
        @SerializedName("vip")
        val vipInfo: VipInfo,
        val pendant: Pendant,
        @SerializedName("decorate_card")
        val decorateCard: DecorateCard?,
        val rank: Int,
        val sign: String,
        @SerializedName("level_info")
        val levelInfo: LevelInfo
) {
    data class DecorateCard(
            val mid: Long,
            val id: Int,
            @SerializedName("card_url")
            val cardUrl: String,
            @SerializedName("card_type")
            val cardType: Int,
            @SerializedName("name")
            val cardName: String,
            /** 永久挂件为0 */
            @SerializedName("expire_time")
            val expireTime: Long,
            /** 卡片分类，如大会员 */
            @SerializedName("card_type_name")
            val cardTypeName: String,
            @SerializedName("uid")
            val uid: Long,
            @SerializedName("item_id")
            val itemId: Int,
            @SerializedName("item_type")
            val itemType: Int,
            @SerializedName("big_card_url")
            val bigCardUrl: String,
            @SerializedName("jump_url")
            val jumpUrL: String,
            /** 粉丝挂件，如湊-阿库娅 */
            @SerializedName("fan")
            val fan: Fan,
            @SerializedName("image_enhance")
            val enhancedImage: String
    ) {
        data class Fan(
                @SerializedName("is_fan")
                val isFan: Int,
                @SerializedName("number")
                val number: Int,
                @SerializedName("color")
                val color: String,
                @SerializedName("num_desc")
                val numberDescription: String
        )
    }

    data class Info(
            var uid: Int = 0,
            @SerializedName("uname")
            var userName: String?,
            @SerializedName("face")
            var avatarImgURL: String?
    )

    data class Card(
            @SerializedName("official_verify")
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
            @SerializedName("image_enhance")
            val imageEnhance: String
    )

    data class LevelInfo(
            @SerializedName("current_level")
            val currentLevel: Int,
            @SerializedName("current_min")
            val currentMinLevel: Int,
            @SerializedName("current_exp")
            val currentExp: Int,
            @SerializedName("next_exp")
            val nextExp: Int
    )

    data class VipInfo(
            val vipType: Int,
            val vipDueDate: Long,
            val dueRemark: String,
            val accessStatus: Int,
            val vipStatus: Int,
            val vipStatusWarn: String,
            val themeType: Int,
            val label: JsonObject?
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