package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.commands.CommandExecutor.doFilter
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.verboseS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.uploadAsImage
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture
import kotlin.time.ExperimentalTime

object TweetUpdateChecker : CometPusher {
    private val pushPool = mutableMapOf<String, PushedTweet>()
    override val delayTime: Long = cfg.twitterInterval
    override val internal: Long = cfg.twitterInterval
    override var future: ScheduledFuture<*>? = null

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

                    if (tweet != null && !isOutdatedTweet(tweet, pushedTweet.tweet) && !tweet.contentEquals(pushedTweet.tweet)) {
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
            val group = bot.getGroupOrNull(it)
            if (group != null) {
                val image = content.getPictureUrl()?.let { url -> NetUtil.getUrlInputStream(url)?.uploadAsImage(group) }
                val filtered = PlainText("${content.user.name} 发布了一条推文\n") + content.convertToString().convertToChain().doFilter()
                try {
                    if (image != null) group.sendMessage(filtered + image)
                    else group.sendMessage(filtered)
                    successCount++
                    delay(2_500)
                } catch (t: Throwable) {
                    daemonLogger.verboseS("Push tweet failed, ${t.message}")
                }
            }
        }

        return successCount
    }

    private data class PushedTweet(val groupsToPush: MutableSet<Long>, var hasPushed: Boolean) {
        var tweet: Tweet? = null
    }

    private fun isOutdatedTweet(retrieve: Tweet, previous: Tweet?): Boolean {
        val isTooOld = Duration.between(retrieve.getSentTime(), LocalDateTime.now()).toMinutes() >= 30
        var isShortInterval = false
        previous?.let {
            isShortInterval = Duration.between(it.getSentTime(), retrieve.getSentTime()).toMinutes() >= 5
        }
        return isTooOld || isShortInterval
    }
}