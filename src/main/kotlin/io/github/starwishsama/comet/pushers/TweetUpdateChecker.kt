package io.github.starwishsama.comet.pushers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.commands.CommandExecutor.doFilter
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.isContentNotEmpty
import net.mamoe.mirai.message.uploadAsImage
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture
import kotlin.time.ExperimentalTime

object TweetUpdateChecker : CometPusher {
    private val pushContent = mutableMapOf<String, PushedTweet>()
    override val delayTime: Long = 5
    override val cycle: Long = 10
    override var future: ScheduledFuture<*>? = null

    @ExperimentalTime
    override fun retrieve() {
        if (!bot.isOnline) future?.cancel(false)

        BotVariables.perGroup.parallelStream().forEach { cfg ->
            if (cfg.twitterPushEnabled) {
                cfg.twitterSubscribers.forEach {
                    if (pushContent.containsKey(it)) {
                        pushContent[it]?.groupsToPush?.add(cfg.id)
                    } else {
                        pushContent[it] = PushedTweet(mutableSetOf(cfg.id), false)
                    }
                }
            }
        }

        pushContent.forEach {
            try {
                val tweet = TwitterApi.getCachedTweet(it.key, max = 1)
                val previousTweet = pushContent[it.key]?.tweet

                if (tweet != null && !isOutdatedTweet(tweet, previousTweet)) {
                    it.value.tweet = tweet
                    TwitterApi.addCacheTweet(it.key, tweet)
                }
            } catch (t: Throwable) {
                if (!NetUtil.isTimeout(t)) {
                    when (t) {
                        is RateLimitException -> logger.warning(t.message)
                        else -> logger.warning("[推文] 在尝试获取推文时出现了意外", t)
                    }
                } else {
                    logger.verbose("[推文] 获取推文时连接超时")
                }
            }
        }

        push()
    }

    @ExperimentalTime
    override fun push() {
        pushContent.forEach { (_, pushObject) ->
            val tweet = pushObject.tweet
            if (tweet != null && !pushObject.isPushed) pushToGroups(pushObject.groupsToPush, tweet)
        }
    }

    @ExperimentalTime
    private fun pushToGroups(groupsToPush: MutableSet<Long>, content: Tweet) {
        groupsToPush.forEach {
            runBlocking {
                val group = bot.getGroupOrNull(it)
                if (group != null) {
                    val image = NetUtil.getUrlInputStream(content.getPictureUrl())?.uploadAsImage(group)
                    val filtered = (PlainText("${content.user.name}\n") + content.getFullText().convertToChain() + (image
                            ?: EmptyMessageChain)).doFilter()
                    if (filtered.isContentNotEmpty()) group.sendMessage(filtered)
                }
            }
        }
    }

    data class PushedTweet(val groupsToPush: MutableSet<Long>, var isPushed: Boolean) {
        var tweet: Tweet? = null
    }

    private fun isOutdatedTweet(retrieve: Tweet, toCompare: Tweet?): Boolean {
        var rate = 0
        val retrieveTime = Duration.between(retrieve.getSentTime(), LocalDateTime.now()).toMinutes()
        if (toCompare != null && retrieve.contentEquals(toCompare)) rate++
        if (retrieveTime >= 45 || (toCompare != null && Duration.between(
                        toCompare.getSentTime(),
                        retrieve.getSentTime()
                ).toMinutes() >= 15)
        ) rate++
        if (toCompare?.let { retrieve.contentEquals(it) } == true) rate++
        return rate > 1
    }
}