package io.github.starwishsama.comet.genshin.gacha.pipeline.env

@kotlinx.serialization.Serializable
class DestinyPipeEnvironment: PipeEnvironment() {

    var weaponDestiny = 0
    var weaponDestinyValue = 0

    override fun resetVariables() {
        weaponDestiny = 0
        weaponDestinyValue = 0
    }

    fun resetWeaponDestiny() {
        weaponDestiny = 0
        weaponDestinyValue = 0
        save()
    }

}