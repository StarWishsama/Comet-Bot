package io.github.starwishsama.comet.objects.draw.items

import com.google.gson.annotations.SerializedName

/**
 * 明日方舟干员
 */
data class ArkNightOperator(
        override val name: String,
        @SerializedName("desc")
        val description: String,
        override val rare: Int = 0,
        val prof: String,
        val tagList: List<String>,
        val obtain: List<String>,
        val birth: String,
        val logo: String,
        val team: String,
        val race: List<String>,
        val build: List<String>,
        val birthday: String,
        val sex: String,
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