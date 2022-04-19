@file:UseSerializers(ItemFastSerializer::class)

package io.github.starwishsama.comet.genshin.gacha.pool

import kotlinx.serialization.UseSerializers
import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaResult
import io.github.starwishsama.comet.genshin.gacha.data.item.Item
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemFastSerializer
import io.github.starwishsama.comet.genshin.gacha.pipeline.GachaPipeline
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.PipeEnvironmentCache
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.StabilizePipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.StarPipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.UpPipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.pipe.*

@kotlinx.serialization.Serializable
class CharacterPool(override val upFourStarList: List<Item>,
                    override val upFiveStarList: List<Item>) : GachaPool() {

    private val starProb: ProbIncreasingStrategy = DefaultProbIncreasingStrategy.characterStar
    private val upProb: UpStrategy = DefaultUpStrategy.characterUp
    private val stabilizeProb: ProbIncreasingStrategy = DefaultProbIncreasingStrategy.stabilize

    override fun getPoolType(): PoolType = PoolType.CHARACTER

    override fun reset(uid: Long) {
        PipeEnvironmentCache.fromUID(uid).getAllEnvironment().forEach { (key, env) ->
            when (key) {
                STABLE_IDENTIFIER -> env.reset()
                STAR_IDENTIFIER -> env.reset()
                UP_IDENTIFIER -> env.reset()
            }
        }
    }

    override fun gacha(uid: Long): GachaResult {
//        val env = CommonGachaPipeEnvironment.fromUID(uid, getPoolType())
//        val gachaTransitionResult = GachaPipeline(env, this) {
//            with(StarPipe())
//            with(UpPipe())
//            with(StabilizePipe())
//        }.gacha()

        val gachaTransitionResult = GachaPipeline(uid) {
            install(StabilizePipeEnvironment(), STABLE_IDENTIFIER) {
                install(StarPipeEnvironment(), STAR_IDENTIFIER) {
                    with(StarPipe(starProb))
                }
                install(UpPipeEnvironment(), UP_IDENTIFIER) {
                    with(UpPipe(upProb))
                }

                with(StabilizePipe(stabilizeProb))
            }
        }.gacha()

        return getFinalResult(gachaTransitionResult, uid)
    }

    companion object {

        const val STABLE_IDENTIFIER = "CharacterPoolStabilize"
        const val STAR_IDENTIFIER = "CharacterPoolStarPipe"
        const val UP_IDENTIFIER = "CharacterPoolUpPipe"

    }

}