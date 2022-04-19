package io.github.starwishsama.comet.genshin.gacha.pipeline.env

@kotlinx.serialization.Serializable
class StarPipeEnvironment: PipeEnvironment() {

    var fourStarCounter = 1
    var fiveStarCounter = 1

    override fun resetVariables() {
        fourStarCounter = 1
        fiveStarCounter = 1
    }

}