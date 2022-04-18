package io.github.starwishsama.comet.genshin.gacha.pipeline.pipe

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaTransitionResult
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.DestinyPipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.StabilizePipeEnvironment

class DestinyPipe: GachaPipe() {

    override fun input(result: GachaTransitionResult): GachaTransitionResult {
        require(result.star != null) { "Star must be determined before the WeaponDestinyPipe" }
        require(result.star != ItemStar.THREE) { "3* could not pass the WeaponDestinyPipe" }

        val env = this.getEnvironment<DestinyPipeEnvironment>()

        if (result.star == ItemStar.FIVE && env.weaponDestiny != 0 && env.weaponDestinyValue >= 2) {
            result.isDestiny = true
            env.getParentEnvironment()?.let {
                if (it is StabilizePipeEnvironment) {
                    it.fiveStarWeaponTypeCounter()
                    it.save()
                }
            }
            env.weaponDestinyValue = 0
            result.isEnd = true
        }

        env.save()

        return result
    }


}