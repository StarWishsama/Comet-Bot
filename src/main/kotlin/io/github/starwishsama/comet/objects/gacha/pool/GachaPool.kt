package io.github.starwishsama.comet.objects.gacha.pool

import io.github.starwishsama.comet.objects.gacha.GachaResult
import io.github.starwishsama.comet.objects.gacha.items.GachaItem

/**
 * 抽卡模拟器中的卡池, 与大部分手游相同
 * 可以设置不同星级的物品的概率, 保底等
 *
 * 由于不同模拟器的抽卡实现不同, 因此概率请自行在抽卡方法中实现
 * 不做统一限制.
 */
abstract class GachaPool {
    /**
     * 游戏卡池名称
     */
    abstract val name: String

    /**
     * 游戏卡池描述
     */
    abstract val description: String

    /**
     * 概率UP的物品, 将会在选择卡池时展示给用户
     */
    val highProbabilityItems: MutableMap<GachaItem, Double> = mutableMapOf()

    /**
     * 天井, 亦称保底次数
     * 这里的保底仅做了简单处理
     * 你也可以不使用这个变量
     * 在抽卡逻辑实现处自行实现保底机制
     *
     * 在多少抽后必出指定星数的物品
     */
    abstract val tenjouCount: Int

    /**
     * 天井, 亦称保底次数
     * 这里的保底仅做了简单处理
     * 你也可以不使用这个变量
     * 在抽卡逻辑实现处自行实现保底机制
     *
     * 在保底后必出的指定星数
     */
    abstract val tenjouRare: Int

    /**
     * 该卡池能够抽到的物品
     */
    abstract val poolItems: MutableList<out GachaItem>

    /**
     * 抽卡实现方法
     */
    abstract fun doDraw(time: Int = 1): GachaResult

    /**
     * 获取指定稀有度的物品
     */
    abstract fun getGachaItem(rare: Int): GachaItem
}