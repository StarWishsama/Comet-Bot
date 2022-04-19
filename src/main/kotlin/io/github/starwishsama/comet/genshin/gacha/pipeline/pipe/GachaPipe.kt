package io.github.starwishsama.comet.genshin.gacha.pipeline.pipe

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaTransitionResult
import io.github.starwishsama.comet.genshin.gacha.pipeline.GachaPipelineBuildHelper
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.PipeEnvironment


abstract class GachaPipe : GachaPipeImpl {

    var gachaPipelineBuildHelper: GachaPipelineBuildHelper? = null
        private set
    var environmentIdentifier: String = ""
        private set

    fun init(envId: String, gachaPipelineBuildHelper: GachaPipelineBuildHelper) {
        this.environmentIdentifier = envId
        this.gachaPipelineBuildHelper = gachaPipelineBuildHelper
    }

    inline fun <reified T: PipeEnvironment> getEnvironment(): T {
        return PipeEnvironment.fromUID(gachaPipelineBuildHelper?.uid?:-1L, environmentIdentifier)
    }

    fun getSafeEnvironment(): PipeEnvironment? {
        return PipeEnvironment.safeFromUID(gachaPipelineBuildHelper?.uid?:-1L, environmentIdentifier)
    }

}

interface GachaPipeImpl {

    fun input(result: GachaTransitionResult): GachaTransitionResult

}