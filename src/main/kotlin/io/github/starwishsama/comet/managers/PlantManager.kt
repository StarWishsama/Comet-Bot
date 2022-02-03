package io.github.starwishsama.comet.managers

import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.plant.PLANTS
import io.github.starwishsama.comet.objects.plant.Plant
import io.github.starwishsama.comet.objects.plant.PlantConfig
import io.github.starwishsama.comet.utils.createNewFileIfNotExists
import io.github.starwishsama.comet.utils.parseAsClass
import io.github.starwishsama.comet.utils.writeClassToJson

import java.io.File

object PlantManager {
    private val PLANT_FILE = File("./plants.json")
    private var cfg = PlantConfig()

    fun load() {
        PLANT_FILE.createNewFileIfNotExists {
            it.writeClassToJson(cfg)
        }

        cfg = PLANT_FILE.parseAsClass()

        cfg.plants.apply {
            clear()
            addAll(cfg.plants)
        }
    }

    fun claimPlant(user: CometUser) {
        PLANTS.random()
    }

    fun getPlantByUser(user: CometUser): Plant? = cfg.plants.find { it.owner == user.uuid }
}