package io.github.starwishsama.comet.objects.draw.items

data class GenshinItem(override val name: String, override val rare: Int, override val count: Int) : GachaItem()