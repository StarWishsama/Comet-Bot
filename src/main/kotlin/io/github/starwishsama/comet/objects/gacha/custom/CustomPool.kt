package io.github.starwishsama.comet.objects.gacha.custom

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment

@Serializable
@Suppress("SpellCheckingInspection")
data class CustomPool(
    @Comment("卡池游戏类型, 目前仅支持 ARKNIGHT, PCR")
    val gameType: GameType,
    @Comment("自定义卡池内部名称, 用于卡池选择")
    val poolName: String,
    @Comment("自定义卡池展示名称")
    val displayPoolName: String,
    @Comment("自定义卡池简介")
    val poolDescription: String,
    @Comment(
        """卡池内不需要干员的条件, 如 "公开招募" 则会屏蔽掉公招可获得的干员
        你也可以在 modifiedGachaItems 处手动添加不需要的卡池物品
    """
    )
    val condition: List<String>,
    @Comment("修改卡池物品的获得概率")
    val modifiedGachaItems: List<ModifiedGachaItem>
) {
    @Serializable
    @Suppress("unused")
    enum class GameType {
        ARKNIGHT, PCR
    }

    @Serializable
    data class ModifiedGachaItem(
        @Comment("修改数据的卡池物品名称")
        val name: String,
        @Comment("该物品的概率提升或降低值, 设为 0 不调整")
        val probability: Double,
        @Comment("是否从卡池中移除该物品, 若不则填 false")
        val isHidden: Boolean
    )
}