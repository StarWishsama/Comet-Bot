package io.github.starwishsama.comet.objects.draw.items

/**
 * 抽卡模拟器中产出的物品. 可以是角色, 装备等.
 */
abstract class GachaItem {
    /**
     * 物品名称
     */
    abstract val name: String

    /**
     * 稀有度, 按照数字排列
     * 在不同游戏下有不同的规则
     */
    abstract val rare: Int

    /**
     * 物品数量
     */
    abstract val count: Int
}