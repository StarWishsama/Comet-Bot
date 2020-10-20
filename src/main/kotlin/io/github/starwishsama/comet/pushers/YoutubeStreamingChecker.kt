package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi
import io.github.starwishsama.comet.api.thirdparty.youtube.YoutubeApi.getLiveItemOrNull
import io.github.starwishsama.comet.objects.pojo.youtube.SearchVideoResult
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.getGroupOrNull
import java.util.concurrent.ScheduledFuture

object YoutubeStreamingChecker : CometPusher {
    override val delayTime: Long = 10
    override val internal: Long = 10
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null
    val pushPool = mutableMapOf<String, PushObject>()

    override fun retrieve() {
        BotVariables.perGroup.forEach { config ->
            if (config.youtubePushEnabled && config.youtubeSubscribers.isNotEmpty()) {
                config.youtubeSubscribers.forEach {
                    if (pushPool.containsKey(it)) {
                        pushPool[it]?.groups?.add(config.id)
                    } else {
                        pushPool[it] = PushObject(mutableListOf(config.id))
                    }
                }
            }
        }

        pushPool.forEach { (chId, pushObj) ->
            val channelInfo = YoutubeApi.getChannelVideos(chId)

            if (channelInfo != null) {
                val old = pushObj.result?.getLiveItemOrNull()
                val now = channelInfo.getLiveItemOrNull()

                if (old == null || old.getVideoUrl() != now?.getVideoUrl()) {
                    pushObj.result = channelInfo
                    pushObj.isPushed = false
                }
            }
        }

        push()
    }

    override fun push() {
        pushPool.forEach { (_, pushObject) ->
            if (!pushObject.isPushed) {
                val wrappedMessage = YoutubeApi.getLiveStatusByResult(pushObject.result)
                pushObject.groups.forEach {
                    val group = bot?.getGroupOrNull(it)
                    runBlocking {
                        try {
                            group?.sendMessage(wrappedMessage.toMessageChain(group))
                            delay(2_500)
                        } catch (t: Throwable) {
                            daemonLogger.verboseS("Push youtube live status failed, ${t.message}")
                        }
                    }
                }
                pushObject.isPushed = true
            }
        }
    }

    data class PushObject(val groups: MutableList<Long>, var isPushed: Boolean = false) {
        var result: SearchVideoResult? = null
    }
}