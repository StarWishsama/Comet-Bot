package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.api.thirdparty.bilibili.MainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.DynamicData
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import java.util.concurrent.ScheduledFuture

object BiliDynamicChecker : CometPusher {
    private val pushedList = mutableSetOf<PushDynamicHistory>()
    override val delayTime: Long = 3
    override val internal: Long = 3
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null

    override fun retrieve() {
        runBlocking { MainApi.getDynamic(-1) }

        push()
    }

    override fun push() {
        TODO("Not yet implemented")
    }

    private data class PushDynamicHistory(
            val pushContent: DynamicData,
            val target: MutableSet<Long>
    ) {
        fun compare(other: PushDynamicHistory): Boolean {
            return runBlocking { pushContent.compare(other.pushContent) }
        }
    }
}