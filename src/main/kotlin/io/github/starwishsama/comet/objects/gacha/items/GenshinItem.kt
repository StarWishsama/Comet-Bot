package io.github.starwishsama.comet.objects.gacha.items

data class GenshinItem(override val name: String, override val rare: Int, override val count: Int) : GachaItem()