@file:UseSerializers(ItemFastSerializer::class, PoolFastSerializer::class)

package io.github.starwishsama.comet.genshin.gacha.data.gacha

import io.github.starwishsama.comet.genshin.gacha.data.item.Item
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemFastSerializer
import io.github.starwishsama.comet.genshin.gacha.data.item.PoolFastSerializer
import io.github.starwishsama.comet.genshin.gacha.pool.GachaPool
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class GachaResult(
    val timestamp: Long,
    val pool: GachaPool,
    val item: Item,
) {

    override fun toString(): String {
        return "${item.itemStar.star}* ${item.itemName}"
    }

}
