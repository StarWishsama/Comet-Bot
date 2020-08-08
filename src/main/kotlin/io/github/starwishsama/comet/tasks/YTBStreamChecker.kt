package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.api.youtube.YoutubeApi
import io.github.starwishsama.comet.api.youtube.YoutubeApi.getLiveItemOrNull
import io.github.starwishsama.comet.objects.pojo.youtube.SearchVideoResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.getGroupOrNull
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture

object YTBStreamChecker : CometPusher {
    override val delayTime: Long = 10
    override val cycle: Long = 10
    override var future: ScheduledFuture<*>? = null
    private val pushHistory = mutableListOf<PushObject>()

    override fun retrieve() {
        if (!bot.isOnline) future?.cancel(true)
        val ytbLiverList = mutableMapOf<String, MutableList<Long>>()

        BotVariables.perGroup.parallelStream().forEach { config ->
            if (config.youtubePushEnabled && config.youtubeSubscribers.isNotEmpty()) {
                config.youtubeSubscribers.forEach {
                    if (ytbLiverList.containsKey(it)) {
                        ytbLiverList[it]?.add(config.id)
                    } else {
                        ytbLiverList[it] = mutableListOf()
                    }
                }
            }
        }

        ytbLiverList.forEach { (name, groups) ->
            val result = YoutubeApi.getChannelVideos(name)
            if (result != null) {
                pushHistory.forEach history@{
                    val history = it.result.getLiveItemOrNull()
                    val now = result.getLiveItemOrNull()
                    if (now?.getChannelId() == history?.getChannelId() && now?.getVideoUrl() != history?.getVideoUrl()) {
                        pushHistory.add(PushObject(result, groups))
                        return@forEach
                    }
                }
                pushHistory.add(PushObject(result, groups))
            }
        }

        push()
    }

    override fun push() {
        pushHistory.forEach { pushObject ->
            pushObject.groups.forEach {
                val wrappedMessage = YoutubeApi.getLiveStatusByResult(pushObject.result)
                val group = bot.getGroupOrNull(it)
                GlobalScope.launch {
                    group?.sendMessage(wrappedMessage.toMessageChain(group))
                    delay(2_500)
                }
            }
            pushObject.isPushed = true
        }
    }

    data class PushObject(val result: SearchVideoResult, val groups: MutableList<Long>, var isPushed: Boolean = false, val retrievedTime: LocalDateTime = LocalDateTime.now())
}