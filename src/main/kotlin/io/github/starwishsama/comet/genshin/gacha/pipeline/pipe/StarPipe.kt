package io.github.starwishsama.comet.genshin.gacha.pipeline.pipe

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaTransitionResult
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.StabilizePipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.StarPipeEnvironment
import kotlin.random.Random
import kotlin.random.nextInt

class StarPipe(private val probIncreasingStrategy: ProbIncreasingStrategy) : GachaPipe() {

    override fun input(result: GachaTransitionResult): GachaTransitionResult {
        val env = getEnvironment<StarPipeEnvironment>()
        val max = probIncreasingStrategy.max
        val weightFiveStarTemp = probIncreasingStrategy.fiveStarStrategy.first { it.range.contains(env.fiveStarCounter) }.let{ it.bias + it.coefficient * (env.fiveStarCounter - (it.range.first - 1)) }
        val weightFourStarTemp = probIncreasingStrategy.fourStarStrategy.first { it.range.contains(env.fourStarCounter) }.let{ it.bias + it.coefficient * (env.fourStarCounter - (it.range.first - 1)) }
        val weightFiveStar = if (weightFiveStarTemp > max) max else weightFiveStarTemp
        val weightFourStar = if (weightFourStarTemp > max) max else weightFourStarTemp
        val secondPoint = weightFiveStar + weightFourStar
        val starRand = Random.nextInt(1..max)
        if (starRand <= weightFiveStar) {
            env.fiveStarCounter = 1
            env.fourStarCounter++
            result.star = ItemStar.FIVE
        } else if (starRand <= secondPoint) {
            env.fiveStarCounter++
            env.fourStarCounter = 1
            result.star = ItemStar.FOUR
        } else {
            env.fiveStarCounter++
            env.fourStarCounter++
            result.star = ItemStar.THREE
            env.getParentEnvironment().also {
                if (it is StabilizePipeEnvironment) {
                    it.threeStarTypeCounter()
                    it.save()
                }
            }
            result.isEnd = true
        }
        env.save()
        return result
    }


}

object DefaultProbIncreasingStrategy {

    val weaponStar = ProbIncreasingStrategy(fiveStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 62, 70, 0),
        ProbIncreasingStrategyUnit(63, 73, 70, 700),
        ProbIncreasingStrategyUnit(74, 1000, 7770, 350)
    ), fourStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 7, 600, 0),
        ProbIncreasingStrategyUnit(8, 100, 6600, 3000)
    ))

    val characterStar = ProbIncreasingStrategy(fiveStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 73, 60, 0),
        ProbIncreasingStrategyUnit(74, 1000, 60, 600)
    ), fourStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 8, 510, 0),
        ProbIncreasingStrategyUnit(9, 100, 510, 5100)
    ))

    val stabilize = ProbIncreasingStrategy(fiveStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 147, 30, 0),
        ProbIncreasingStrategyUnit(148, 10000, 30, 300)
    ), fourStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 17, 255, 0),
        ProbIncreasingStrategyUnit(18, 1000, 255, 2550)
    ))

    val weaponStabilize = ProbIncreasingStrategy(fiveStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 147, 30, 0),
        ProbIncreasingStrategyUnit(148, 10000, 30, 300)
    ), fourStarStrategy = listOf(
        ProbIncreasingStrategyUnit(0, 15, 300, 0),
        ProbIncreasingStrategyUnit(16, 1000, 300, 3000)
    ))

}

@kotlinx.serialization.Serializable
data class ProbIncreasingStrategy(
    val max: Int = 10000,
    val fiveStarStrategy: List<ProbIncreasingStrategyUnit>,
    val fourStarStrategy: List<ProbIncreasingStrategyUnit>
)

@kotlinx.serialization.Serializable
data class ProbIncreasingStrategyUnit(
    val start: Int,
    val end: Int,
    val bias: Int,
    val coefficient: Int
) {
    val range by lazy { start..end }
}
