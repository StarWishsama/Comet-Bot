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
import io.github.starwishsama.comet.genshin.gacha.pipeline.pipe.*

@kotlinx.serialization.Serializable
class PermanentPool(override val upFourStarList: List<Item>,
                    override val upFiveStarList: List<Item>) : GachaPool(){

    override fun getPoolType(): PoolType = PoolType.PERMANENT

    private val starProb: ProbIncreasingStrategy = DefaultProbIncreasingStrategy.characterStar
    private val stabilizeProb: ProbIncreasingStrategy = DefaultProbIncreasingStrategy.stabilize


    override fun reset(uid: Long) {
        PipeEnvironmentCache.fromUID(uid).getAllEnvironment().forEach { (key, env) ->
            when (key) {
                STABLE_IDENTIFIER -> env.reset()
                STAR_IDENTIFIER -> env.reset()
            }
        }
    }


    override fun gacha(uid: Long): GachaResult {
//        val env = CommonGachaPipeEnvironment.fromUID(uid, getPoolType())
//        val gachaTransitionResult = GachaPipeline(env, this) {
//            with(StarPipe())
//            with(StabilizePipe())
//        }.gacha()
//        env.save()

        val gachaTransitionResult = GachaPipeline(uid) {
            install(StabilizePipeEnvironment(), STABLE_IDENTIFIER) {
                install(StarPipeEnvironment(), STAR_IDENTIFIER) {
                    with(StarPipe(starProb))
                }
                with(StabilizePipe(stabilizeProb))
            }
        }.gacha()

        return getFinalResult(gachaTransitionResult, uid)
    }


    companion object {

        const val STABLE_IDENTIFIER = "PermanentPoolStabilize"
        const val STAR_IDENTIFIER = "PermanentPoolStarPipe"

    }

}