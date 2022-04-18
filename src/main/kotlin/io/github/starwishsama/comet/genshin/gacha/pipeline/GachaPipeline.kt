package io.github.starwishsama.comet.genshin.gacha.pipeline

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaTransitionResult
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.GlobalGachaPipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.PipeEnvironment
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.PipeEnvironmentCache
import io.github.starwishsama.comet.genshin.gacha.pipeline.env.PipeEnvironmentImpl
import io.github.starwishsama.comet.genshin.gacha.pipeline.pipe.GachaPipe
import java.util.*


class GachaPipeline (
    uid: Long,
    pipelineInstaller: GachaPipelineBuildHelper.() -> Unit
) {

    private val gachaPipelineBuildHelper = GachaPipelineBuildHelper(uid, this, GlobalGachaPipeEnvironment.fromUID(uid),GlobalGachaPipeEnvironment.GLOBAL_IDENTIFIER)

    init {
        pipelineInstaller(gachaPipelineBuildHelper)
    }

    fun gacha(): GachaTransitionResult {
        return gachaPipelineBuildHelper.gacha(GachaTransitionResult(), arrayListOf())
    }

}

class GachaPipelineBuildHelper(
    val uid: Long,
    private val pipeline: GachaPipeline,
    private val defaultEnvironment: PipeEnvironment,
    private var environmentIdentifier: String,
    private val parentBuilder: GachaPipelineBuildHelper? = null
) {

    private val environment: PipeEnvironmentImpl by lazy {
        PipeEnvironmentCache.fromUID(uid).getEnvironment(environmentIdentifier, defaultEnvironment)
    }

    private val gachaEnvironment: LinkedList<GachaPipelineBuildHelper> = LinkedList()
    private val installedPipe: LinkedList<GachaPipe> = LinkedList()
    private val order: LinkedList<InstallType> = LinkedList()

    fun install(environment: PipeEnvironment, identifier: String, pipelineInstaller: GachaPipelineBuildHelper.() -> Unit) {
        val cache = PipeEnvironmentCache.fromUID(uid)
        cache.addEnvironment(uid, identifier, environment, this.environmentIdentifier)
        val gachaPipelineBuildHelper = GachaPipelineBuildHelper(uid, pipeline, environment, identifier, this)
        pipelineInstaller(gachaPipelineBuildHelper)
        gachaEnvironment.add(gachaPipelineBuildHelper)
        order.add(InstallType.ENVIRONMENT)
    }

    fun getParentBuilder(): GachaPipelineBuildHelper? = parentBuilder

    fun with(pipe: GachaPipe) {
        pipe.init(environmentIdentifier, this)
        installedPipe.add(pipe)
        order.add(InstallType.PIPE)
    }

    fun gacha(result: GachaTransitionResult, visitedPipe: ArrayList<GachaPipe>): GachaTransitionResult {
        var lastResult = result
        order.forEach { type ->
            when (type) {
                InstallType.PIPE -> {
                    installedPipe.forEach { pipe ->
                        if (!visitedPipe.contains(pipe)) {
                            lastResult = pipe.input(lastResult)
                            visitedPipe.add(pipe)
                            if (lastResult.isEnd) {
                                return lastResult
                            }
                        }
                    }
                }
                InstallType.ENVIRONMENT -> {
                    gachaEnvironment.forEach { helper ->
                        helper.gacha(lastResult, visitedPipe)
                        if (lastResult.isEnd) return lastResult
                    }
                }
            }
        }

        return lastResult
    }

    enum class InstallType {
        PIPE,
        ENVIRONMENT
    }

}

//fun main() {
//    GachaPoolManager.init()
//    val scanner = Scanner(System.`in`)
//    while (scanner.nextLine() != "stop") {
//        try {
//            repeat(10) {
//                println(GachaPoolManager.getGachaPools().first().gacha(114514L))
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}