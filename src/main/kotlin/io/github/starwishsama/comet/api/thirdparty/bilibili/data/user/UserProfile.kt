package io.github.starwishsama.comet.api.thirdparty.bilibili.data.user

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class UserProfile(
        val info: Info,
        val card: Card,
        @SerializedName("vip")
        val vipInfo: VipInfo,
        val pendant: Pendant,
        val rank: Int,
        val sign: String,
        @SerializedName("level_info")
        val levelInfo: LevelInfo
) {
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