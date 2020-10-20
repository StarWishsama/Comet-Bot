package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.command.CommandExecutor.doFilter
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.PlainText
import java.util.concurrent.ScheduledFuture
import kotlin.time.ExperimentalTime

object TweetUpdateChecker : CometPusher {
    val pushPool = mutableMapOf<String, PushedTweet>()
    override val delayTime: Long = cfg.twitterInterval
    override val internal: Long = cfg.twitterInterval
    override var future: ScheduledFuture<*>? = null
    override var bot: Bot? = null

    @ExperimentalTime
    override fun retrieve() {
        BotVariables.perGroup.parallelStream().forEach { cfg ->
            if (cfg.twitterPushEnabled) {
                cfg.twitterSubscribers.forEach {
                    if (pushPool.containsKey(it)) {
                        pushPool[it]?.groupsToPush?.add(cfg.id)
                    } else {
                        pushPool[it] = PushedTweet(mutableSetOf(cfg.id), false)
                    }
                }
            }
        }

        var count = 0

        pushPool.forEach { (userName, pushedTweet) ->
            TaskUtil.executeWithRetry(2) {
                try {
                    val tweet = TwitterApi.getCachedTweet(username = userName, max = 1)

                    if (tweet != null && !tweet.contentEquals(pushedTweet.tweet)) {
                        pushedTweet.tweet = tweet
                        pushedTweet.hasPushed = false
                        count++
                    }
                } catch (t: Throwable) {
                    if (!NetUtil.isTimeout(t)) {
                        when (t) {
                            is RateLimitException -> daemonLogger.verbose(t.message)
                            else -> daemonLogger.verboseS("[推文] 在尝试获取推文时出现了意外", t)
                        }
                    } else {
                        daemonLogger.verboseS("[推文] 获取推文时连接超时")
                    }
                }
            }
        }

        if (count > 0) daemonLogger.verboseS("Retrieve success, have collected $count tweet(s)!")

        push()
    }

    @ExperimentalTime
    override fun push() {
        var count = 0

        pushPool.forEach { (_, pushObject) ->
            if (!pushObject.hasPushed) {
                pushObject.tweet?.let {
                    GlobalScope.launch {
                        count = pushToGroups(pushObject.groupsToPush, it)
                        pushObject.hasPushed = true
                    }
                }
            }
        }

        if (count > 0) daemonLogger.verboseS("Push success, have pushed $count group(s)!")
    }

    @ExperimentalTime
    private suspend fun pushToGroups(groupsToPush: MutableSet<Long>, content: Tweet): Int {
        var successCount = 0

        groupsToPush.forEach {
            val group = bot?.getGroupOrNull(it)
            if (group != null) {
                val msg = (PlainText("${content.user.name} 发布了一条推文\n") + content.toMessageChain(group)).doFilter()

                try {
                    group.sendMessage(msg)
                    successCount++
                    delay(2_500)
                } catch (t: EventCancelledException) {
                    daemonLogger.verboseS("Push tweet failed, ${t.message}")
                }
            }
        }

        return successCount
    }

    data class PushedTweet(val groupsToPush: MutableSet<Long>, var hasPushed: Boolean) {
        var tweet: Tweet? = null
    }
}