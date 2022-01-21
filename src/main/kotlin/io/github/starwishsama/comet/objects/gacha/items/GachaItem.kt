/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.gacha.items

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