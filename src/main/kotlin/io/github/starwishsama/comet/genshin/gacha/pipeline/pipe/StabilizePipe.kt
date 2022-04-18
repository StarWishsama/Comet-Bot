package io.github.starwishsama.comet.genshin.gacha.pipeline.pipe

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaTransitionResult
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemType
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.StabilizePipeEnvironment
import kotlin.random.Random
import kotlin.random.nextInt

class StabilizePipe(private val probIncreasingStrategy: ProbIncreasingStrategy) : GachaPipe() {

    override fun input(result: GachaTransitionResult): GachaTransitionResult {
        require(result.star != null) { "Star must be determined before the Stabilize" }
        require(result.star != ItemStar.THREE) { "3* could not pass the Stabilize" }
        require(!result.isDestiny) { "Destiny gacha not need to pass Stabilize" }
        require(!result.isUp) { "Up gacha not need to pass Stabilize" }
        require(result.type == null) { "Gacha type could not be determined before the Stabilize" }

        val env = this.getEnvironment<StabilizePipeEnvironment>()
        val calculateWeight: (Int, Int) -> (Int) = { five: Int, four: Int ->
            when (result.star) {
                ItemStar.FIVE -> probIncreasingStrategy.fiveStarStrategy.first { it.range.contains(five) }.let{ it.bias + it.coefficient * (five - (it.range.first - 1)) }
                ItemStar.FOUR -> probIncreasingStrategy.fourStarStrategy.first { it.range.contains(four) }.let{ it.bias + it.coefficient * (four - (it.range.first - 1)) }
                else -> { 0 }
            }
        }
        val characterWeight = calculateWeight(env.fiveStarCharacterCounter, env.fourStarCharacterCounter)
        val weaponWeight = calculateWeight(env.fiveStarWeaponCounter, env.fourStarWeaponCounter)
        val max = characterWeight + weaponWeight
        result.type = if (Random.nextInt(1..max) <= characterWeight) {
            ItemType.CHARACTER
        } else {
            ItemType.WEAPON
        }

        env.save()

        return result
    }


}