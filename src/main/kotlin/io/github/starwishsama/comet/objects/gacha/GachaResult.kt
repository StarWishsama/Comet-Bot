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