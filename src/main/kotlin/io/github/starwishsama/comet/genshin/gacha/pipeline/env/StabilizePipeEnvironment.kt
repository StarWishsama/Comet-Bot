package io.github.starwishsama.comet.genshin.gacha.pipeline.env

@kotlinx.serialization.Serializable
class StabilizePipeEnvironment: PipeEnvironment() {

    var fourStarCharacterCounter = 1
    var fourStarWeaponCounter = 1
    var fiveStarCharacterCounter = 1
    var fiveStarWeaponCounter = 1

    override fun resetVariables() {
        fourStarCharacterCounter = 1
        fourStarWeaponCounter = 1
        fiveStarCharacterCounter = 1
        fiveStarWeaponCounter = 1
    }

    fun threeStarTypeCounter() {
        fourStarCharacterCounter++
        fourStarWeaponCounter++
        fiveStarCharacterCounter++
        fiveStarWeaponCounter++
    }

    fun fourStarCharacterTypeCounter() {
        fourStarCharacterCounter = 1
        fourStarWeaponCounter++
        fiveStarCharacterCounter++
        fiveStarWeaponCounter++
    }

    fun fourStarWeaponTypeCounter() {
        fourStarWeaponCounter = 1
        fourStarCharacterCounter++
        fiveStarCharacterCounter++
        fiveStarWeaponCounter++
    }

    fun fiveStarCharacterTypeCounter() {
        fiveStarCharacterCounter = 1
        fourStarCharacterCounter++
        fourStarWeaponCounter++
        fiveStarWeaponCounter++
    }

    fun fiveStarWeaponTypeCounter() {
        fiveStarWeaponCounter = 1
        fourStarCharacterCounter++
        fourStarWeaponCounter++
        fiveStarCharacterCounter++
    }

}