package io.github.starwishsama.comet.objects.gacha.items

import com.google.gson.annotations.SerializedName

/**
 * 明日方舟干员
 *
 * 格式: 官方角色数据拆包
 */
data class ArkNightOperator(
        override val name: String,
        @SerializedName("desc")
        val description: String,
        /**
         * 星级
         *
         * 注意: 明日方舟官方数据中, 星级从0开始计起
         * 即一星干员获取后为零, 但自定义卡池不受影响
         */
        @SerializedName("rarity")
        override val rare: Int = 0,
        /**
         * 获得途径
         */
        @SerializedName("itemObtainApproach")
        val obtain: String?,
        override val count: Int = 1,
) : GachaItem()