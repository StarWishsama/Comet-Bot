package io.github.starwishsama.comet.genshin.gacha.data.gacha

import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemType

data class GachaTransitionResult(
    var star: ItemStar? = null,
    var type: ItemType? = null,
    var isUp: Boolean = false,
    var isDestiny: Boolean = false,
    var isEnd: Boolean = false,
)