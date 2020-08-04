package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.utils.toMsgChain
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ScheduledFuture
import kotlin.time.ExperimentalTime

object TweetUpdateChecker : CometPusher {
    private val pushContent = mutableMapOf<String, PushedTweet>()
    override val delayTime: Long = 5
    override val cycle: Long = 10
    override lateinit var future: ScheduledFuture<*>

    @ExperimentalTime
    override fun retrieve() {
        if (!bot.isOnline) future.cancel(false)

        /** 检查是否有 Twitter Token, 如无则手动获取 */
        if (TwitterApi.token.isNullOrEmpty()) {
            TwitterApi.getBearerToken()
        }

        /** 创建一个包含所有待获取的推特账号名列表 */
        val subList = HashSet<String>()
        /** 收集所有需推送账户名 */
        BotVariables.perGroup.forEach {
            subList.addAll(it.twitterSubscribers)
        }

        /** 获取推文 */
        subList.parallelStream().forEach {
            try {
                val tweet = TwitterApi.getTweetWithCache(it)
                val oldTweet = pushContent[it]?.tweet

                /** 检查是否为重复推文, 如果不是则加入推送队列 */
                if (tweet != null && !isOutdatedTweet(tweet, oldTweet)) {
                    /** 加入推送队列, 注明尚未推送过 */
                    pushContent[it] = PushedTweet(tweet, false)
                    /** 添加到推文缓存池中 */
                    TwitterApi.addCacheTweet(it, tweet)
                }
            } catch (e: Throwable) {
                if (e is RateLimitException) BotVariables.logger.warning(e.message)
                else BotVariables.logger.warning(e)
            }
        }

        push()
    }

    @ExperimentalTime
    override fun push() {
        /** 待推送推文队列, 包含推主名字和该推主需要推送到的群 */
        val pushQueue = HashMap<String, List<Long>>()

        /** 从分群配置文件中获取需要被推送的群 */
        BotVariables.perGroup.parallelStream().forEach {
            /** 检查该群是否启用了推送功能 */
            if (GroupConfigManager.getConfigSafely(it.id).twitterPushEnabled) {
                for (subs in it.twitterSubscribers) {
                    /** 将群号添加到待推送列表 */
                    if (pushQueue.containsKey(subs) && !pushQueue.isNullOrEmpty()) {
                        pushQueue[subs] = pushQueue[subs]?.plus(it.id) ?: arrayListOf(it.id)
                    } else {
                        pushQueue[subs] = arrayListOf(it.id)
                    }
                }
            }
        }

        pushToGroups(pushQueue)
    }

    data class PushedTweet(val tweet: Tweet, var isPushed: Boolean)

    @ExperimentalTime
    private fun pushToGroups(pushQueue: HashMap<String, List<Long>>) {
        /** 遍历推送列表推送推文 */
        pushQueue.forEach { (userName, pushGroups) ->
            val container = pushContent[userName]
            /** 检查该推文是否被推送过 */
            if (container != null && !container.isPushed) {
                val tweet = container.tweet
                var message = "${tweet.user.name} (@${userName}) 发送了一条推文\n${tweet.getFullText()}".toMsgChain()
                pushGroups.forEach {
                    runBlocking {
                        val group = bot.getGroup(it)
                        val image = tweet.getPictureOrNull(group)
                        if (image != null) {
                            message += image
                        }
                        group.sendMessage(message)
                        delay(2_500)
                    }
                }
                container.isPushed = true
            }
        }
    }

    private fun isOutdatedTweet(retrieve: Tweet, toCompare: Tweet?): Boolean {
        val timeNow = LocalDateTime.now()
        val tweetSentTime = retrieve.getSentTime()
        if (Duration.between(tweetSentTime, timeNow).toMinutes() >= 30) return true

        if (toCompare == null) return false

        return retrieve.contentEquals(toCompare)
    }
}