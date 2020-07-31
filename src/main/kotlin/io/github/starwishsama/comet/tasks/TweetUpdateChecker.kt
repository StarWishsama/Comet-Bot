package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
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
import kotlin.time.ExperimentalTime

object TweetUpdateChecker : CometPusher {
    private val pushContent = mutableMapOf<String, PushedTweet>()
    override val delayTime: Long = 5
    override val cycle: Long = 15

    @ExperimentalTime
    override fun retrieve() {
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
        subList.forEach {
            try {
                val tweet = TwitterApi.getTweetWithCache(it)
                val oldTweet = pushContent[it]?.tweet ?: return@forEach

                /** 检查是否为重复推文, 如果不是则加入推送队列 */
                if (tweet != null && !isOutdatedTweet(tweet, oldTweet)) {
                    /** 加入推送队列, 注明尚未推送过 */
                    pushContent[it] = PushedTweet(tweet, false)
                    /** 添加到缓存池中 */
                    TwitterApi.addCacheTweet(it, tweet)
                }
            } catch (e: RateLimitException) {
                BotVariables.logger.debug(e.localizedMessage)
            }
        }

        push()
    }

    @ExperimentalTime
    override fun push() {
        /** 待推送推文队列, 包含推主名字和该推主需要推送到的群 */
        val pushQueue = HashMap<String, List<Long>>()

        /** 从分群配置文件中获取需要被推送的群 */
        BotVariables.perGroup.forEach {
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

        /** 遍历推送列表推送推文 */
        pushQueue.forEach { (userName, pushGroups) ->
            val container = pushContent[userName]
            /** 检查该推文是否被推送过 */
            if (container != null && !container.isPushed) {
                val tweet = container.tweet
                var message = "${tweet.user.name} (@${userName}) 发送了一条推文\n${tweet.getFullText()}".toMsgChain()
                pushGroups.forEach {
                    runBlocking {
                        val group = BotVariables.bot.getGroup(it)
                        val image = tweet.getPictureOrNull(group)
                        if (image != null) {
                            message += image
                        }
                        group.sendMessage(message)
                        BotVariables.logger.debug("Successfully push latest tweet to ${group.id}")
                        delay(2_500)
                    }
                }
            }

            container?.isPushed = true
        }
    }

    data class PushedTweet(val tweet: Tweet, var isPushed: Boolean)

    private fun isOutdatedTweet(retrieve: Tweet, toCompare: Tweet): Boolean {
        val timeNow = LocalDateTime.now()
        val tweetSentTime = retrieve.getSentTime()
        return Duration.between(tweetSentTime, timeNow).toMinutes() >= 30 || toCompare.contentEquals(retrieve)
    }
}