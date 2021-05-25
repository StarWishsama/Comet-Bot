/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.gacha

import io.github.starwishsama.comet.objects.gacha.items.GachaItem

data class GachaResult(
    /**
     * 抽卡获得物品
     */
    val items: MutableList<GachaItem> = mutableListOf(),
    /**
     * 抽卡获得限定/特级物品, 会特殊展示.
     */
    val specialItems: MutableList<GachaItem> = mutableListOf()
) {
    fun isEmpty(): Boolean {
        return items.isEmpty()
    }
}