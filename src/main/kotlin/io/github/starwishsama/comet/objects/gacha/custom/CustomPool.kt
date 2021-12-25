/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.gacha.custom

import kotlinx.serialization.Serializable

import net.mamoe.yamlkt.Comment

@Serializable
@Suppress("SpellCheckingInspection")
data class CustomPool(
    @Comment("卡池游戏类型, 支持 ARKNIGHT, GENSHIN")
    val gameType: GameType,
    @Comment("自定义卡池内部名称, 用于卡池选择")
    val poolName: String,
    @Comment("自定义卡池开始时间, 格式为 yyyy-MM-dd HH:mm:ss, 留空为无限期")
    val startTime: String,
    @Comment("自定义卡池结束时间, 格式为 yyyy-MM-dd HH:mm:ss, 留空为无限期")
    val endTime: String,
    @Comment("自定义卡池展示名称")
    val displayPoolName: String,
    @Comment("自定义卡池简介")
    val poolDescription: String,
    /** @Comment(
    """卡池内不需要干员的条件, 如 "公开招募" 则会屏蔽掉公招可获得的干员
    你也可以在 modifiedGachaItems 处手动添加不需要的卡池物品
    """
    )*/
    val condition: List<String>,
    @Comment("修改卡池物品的获得概率")
    val modifiedGachaItems: List<ModifiedGachaItem>
) {
    @Serializable
    enum class GameType {
        ARKNIGHT,
        GENSHIN,
    }

    @Serializable
    data class ModifiedGachaItem(
        @Comment("修改数据的卡池物品名称")
        val name: String,
        @Comment("该物品的概率提升或降低值, 设为 0 不调整")
        val probability: Double,
        @Comment("加成权值, 目前仅有明日方舟中使用该特性. 如果对应物品没有权值填 1")
        val weight: Int,
        @Comment("是否从卡池中移除该物品, 若不则填 false")
        val isHidden: Boolean
    )
}