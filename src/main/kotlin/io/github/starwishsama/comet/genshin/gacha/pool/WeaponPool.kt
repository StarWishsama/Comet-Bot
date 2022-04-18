@file:UseSerializers(ItemFastSerializer::class)

package io.github.starwishsama.comet.genshin.gacha.pool

import kotlinx.serialization.UseSerializers
import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaResult
import io.github.starwishsama.comet.genshin.gacha.data.item.Item
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemFastSerializer
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemPool
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.pipeline.GachaPipeline
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.*
import io.github.starwishsama.comet.genshin.gacha.pipeline.pipe.*


@kotlinx.serialization.Serializable
class WeaponPool(
    override val upFourStarList: List<Item>,
    override val upFiveStarList: List<Item>
) : GachaPool() {

    private val starProb: ProbIncreasingStrategy = DefaultProbIncreasingStrategy.weaponStar
    private val upProb: UpStrategy = DefaultUpStrategy.weaponUp
    private val stabilizeProb: ProbIncreasingStrategy = DefaultProbIncreasingStrategy.weaponStabilize

    override fun getPoolType(): PoolType = PoolType.WEAPON

    override fun reset(uid: Long) {
        PipeEnvironmentCache.fromUID(uid).getAllEnvironment().forEach { (key, env) ->
            when (key) {
                STABLE_IDENTIFIER -> env.reset()
                DESTINY_IDENTIFIER -> env.reset()
                STAR_IDENTIFIER -> env.reset()
                UP_IDENTIFIER -> env.reset()
            }
        }
    }

    override fun gacha(uid: Long): GachaResult {
//        val env = CommonGachaPipeEnvironment.fromUID(uid, getPoolType())
//        val gachaTransitionResult = GachaPipeline(env, this) {
//            with(StarPipe())
//            with(DestinyPipe())
//            with(UpPipe())
//            with(StabilizePipe())
//        }.gacha()

        val gachaTransitionResult = GachaPipeline(uid) {
            install(StabilizePipeEnvironment(), STABLE_IDENTIFIER) {
                install(StarPipeEnvironment(), STAR_IDENTIFIER) {
                    with(StarPipe(starProb))
                }
                install(DestinyPipeEnvironment(), DESTINY_IDENTIFIER) {
                    with(DestinyPipe())
                }
                install(UpPipeEnvironment(), UP_IDENTIFIER) {
                    with(UpPipe(upProb))
                }
                with(StabilizePipe(stabilizeProb))
            }
        }.gacha()

        return getFinalResult(gachaTransitionResult, uid, DESTINY_IDENTIFIER)
    }

    fun getDestinyInfo(uid: Long): Pair<Int, Int> {
        val env = PipeEnvironment.fromUID<DestinyPipeEnvironment>(uid, DESTINY_IDENTIFIER)
        return env.weaponDestiny to env.weaponDestinyValue
    }

    fun setDestiny(uid: Long, id: Int) {
        val env = PipeEnvironment.fromUID<DestinyPipeEnvironment>(uid, DESTINY_IDENTIFIER)
        env.weaponDestiny = id
        env.weaponDestinyValue = 0
        env.save()
    }

    companion object {

        const val STABLE_IDENTIFIER = "WeaponPoolStabilize"
        const val STAR_IDENTIFIER = "WeaponPoolDestinyPipe"
        const val DESTINY_IDENTIFIER = "WeaponPoolStarPipe"
        const val UP_IDENTIFIER = "WeaponPoolUpPipe"

    }

}