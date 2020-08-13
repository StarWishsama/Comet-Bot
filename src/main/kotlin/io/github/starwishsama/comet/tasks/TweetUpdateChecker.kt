package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.toMsgChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.EmptyMessageChain
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

        /** 检查是否有 Twitter Token, 如无则手动获取 */
        if (TwitterApi.token == null) {
            TwitterApi.getBearerToken()
        }

        BotVariables.perGroup.parallelStream().forEach { cfg ->
            if (cfg.twitterPushEnabled) cfg.twitterSubscribers.forEach {
                if (pushContent.containsKey(it)) {
                    pushContent[it]?.groupsToPush?.add(cfg.id)
                } else {
                    pushContent[it] = PushedTweet(mutableSetOf(cfg.id), false)
                }
            }
        }

        pushContent.forEach {
            try {
                val tweet = TwitterApi.getCachedTweet(it.key, max = 1).tweet
                val previousTweet = pushContent[it.key]?.tweet

                if (tweet != null && !isOutdatedTweet(tweet, previousTweet)) {
                    it.value.tweet = tweet
                    TwitterApi.addCacheTweet(it.key, tweet)
                }
            } catch (t: Throwable) {
                val message = t.message
                when {
                    t is RateLimitException -> logger.warning(t.message)
                    message != null && message.contains("times") -> logger.verbose("[推文] 获取推文时连接超时")
                    else -> logger.warning("[推文] 在尝试获取推文时出现了意外", t)
                }
            }
        }

        push()
    }

    @ExperimentalTime
    override fun push() {
        pushContent.forEach { (_, pushObject) ->
            val tweet = pushObject.tweet
            if (tweet != null) pushToGroups(pushObject.groupsToPush, tweet)
        }
    }

    @ExperimentalTime
    private fun pushToGroups(groupsToPush: MutableSet<Long>, content: Tweet) {
        groupsToPush.forEach {
            runBlocking {
                val group = bot.getGroupOrNull(it)
                if (group != null) {
                    val image = NetUtil.getUrlInputStream(content.getPictureUrl())?.uploadAsImage(group)
                    group.sendMessage(content.getFullText().toMsgChain() + (image ?: EmptyMessageChain))
                }
            }
        }
    }

    data class PushedTweet(val groupsToPush: MutableSet<Long>, var isPushed: Boolean) {
        var tweet: Tweet? = null
    }

    private fun isOutdatedTweet(retrieve: Tweet, toCompare: Tweet?): Boolean {
        val retrieveTime = Duration.between(retrieve.getSentTime(), LocalDateTime.now()).toMinutes()
        if (retrieveTime >= 45 || (toCompare != null && Duration.between(
                toCompare.getSentTime(),
                retrieve.getSentTime()
            ).toMinutes() >= 60)
        ) return true
        return toCompare?.let { retrieve.contentEquals(it) } ?: false
    }
}