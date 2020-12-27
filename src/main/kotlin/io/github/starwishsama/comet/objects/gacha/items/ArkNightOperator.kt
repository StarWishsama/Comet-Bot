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
        val obtain: String,
        override val count: Int = 1,
) : GachaItem()

/**
{
"name": "迷迭香",
"desc": "攻击对小范围的<@ba.kw>地面</>敌人造成<@ba.kw>两次</>物理伤害（第二次为余震，伤害降低至攻击力的一半）",
"rare": 6,
"prof": "狙击",
"tagList": ["远程位", "输出"],
"obtain": ["限定寻访"],
"birth": "哥伦比亚",
"logo": "罗德岛",
"team": "无团队",
"race": ["菲林"],
"build": ["制造站"],
"birthday": "七月",
"sex": "女"
}
 */