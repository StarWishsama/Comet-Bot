package io.github.starwishsama.comet.objects.draw

/**
 * 抽卡模拟器中产出的物品. 可以是角色, 装备等.
 */
interface GachaItem {
    /**
     * 物品名称
     */
    var name: String

    /**
     * 稀有度, 按照数字排列
     * 在不同游戏下有不同的规则
     */
    var rare: Int
}