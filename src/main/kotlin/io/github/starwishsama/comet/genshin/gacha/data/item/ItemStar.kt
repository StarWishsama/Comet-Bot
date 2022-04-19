package io.github.starwishsama.comet.genshin.gacha.data.item

@kotlinx.serialization.Serializable
enum class ItemStar(val star: Int) {
    FIVE(5),
    FOUR(4),
    THREE(3)
}