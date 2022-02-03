package io.github.starwishsama.comet.objects.plant

data class PlantConfig(
    val versionCode: Int = 1,
    val plants: MutableList<Plant> = mutableListOf()
)
