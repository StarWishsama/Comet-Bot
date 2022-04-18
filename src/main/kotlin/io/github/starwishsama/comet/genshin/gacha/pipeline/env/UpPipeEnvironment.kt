package io.github.starwishsama.comet.genshin.gacha.pipeline.env

@kotlinx.serialization.Serializable
class UpPipeEnvironment: PipeEnvironment() {

    var lastNoUpFiveStar = false
    var lastNoUpFourStar = false

    override fun resetVariables() {
        lastNoUpFiveStar = false
        lastNoUpFourStar = false
    }

}