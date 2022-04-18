package io.github.starwishsama.comet.genshin.gacha.pipeline.pipe

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaTransitionResult
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemType
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.StabilizePipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.UpPipeEnvironment
import kotlin.random.Random
import kotlin.random.nextInt

class UpPipe(private val upStrategy: UpStrategy) : GachaPipe() {

    override fun input(result: GachaTransitionResult): GachaTransitionResult {
        require(result.star != null) { "Star must be determined before the UpPipe" }
        require(result.star != ItemStar.THREE) { "3* could not pass the UpPipe" }
        require(!result.isDestiny) { "Destiny gacha not need to pass UpPipe" }

        val env = this.getEnvironment<UpPipeEnvironment>()
        val stabilizeEnv = env.let { val parentEnv = it.getParentEnvironment(); if (parentEnv is StabilizePipeEnvironment) parentEnv else null }

        if (result.star == ItemStar.FIVE) {
            if (env.lastNoUpFiveStar) {
                env.lastNoUpFiveStar = false
                result.isUp = true
            } else {
                result.isUp = Random.nextInt(upStrategy.randomRange) <= upStrategy.probBorder
                env.lastNoUpFiveStar = !result.isUp
            }
            result.type = upStrategy.upItemType
            if (upStrategy.upItemType == ItemType.CHARACTER) stabilizeEnv?.fiveStarCharacterTypeCounter() else stabilizeEnv?.fiveStarWeaponTypeCounter()
            result.isEnd = true
        } else if (result.star == ItemStar.FOUR) {
            if (env.lastNoUpFourStar) {
                env.lastNoUpFourStar = false
                result.isUp = true
                result.type = upStrategy.upItemType
                if (upStrategy.upItemType == ItemType.CHARACTER) stabilizeEnv?.fourStarCharacterTypeCounter() else stabilizeEnv?.fourStarWeaponTypeCounter()
                result.isEnd = true
            } else {
                result.isUp = Random.nextInt(upStrategy.randomRange) <= upStrategy.probBorder
                env.lastNoUpFourStar = !result.isUp
                if (result.isUp) {
                    result.type = upStrategy.upItemType
                    if (upStrategy.upItemType == ItemType.CHARACTER) stabilizeEnv?.fourStarCharacterTypeCounter() else stabilizeEnv?.fourStarWeaponTypeCounter()
                    result.isEnd = true
                }
            }
        }

        env.save()
        stabilizeEnv?.save()

        return result
    }

}

object DefaultUpStrategy {

    val characterUp = UpStrategy(ItemType.CHARACTER, 0, 1, 0)
    val weaponUp = UpStrategy(ItemType.WEAPON, 0, 3, 2)

}

@kotlinx.serialization.Serializable
data class UpStrategy(
    val upItemType: ItemType,
    val start: Int,
    val end: Int,
    val probBorder: Int,
) {
    val randomRange by lazy {
        start..end
    }
}

fun Int.toBoolean() = this == 1