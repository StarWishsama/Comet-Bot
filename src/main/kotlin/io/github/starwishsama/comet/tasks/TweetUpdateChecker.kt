package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.utils.toMsgChain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.ExperimentalTime

object TweetUpdateChecker : CometPusher {
    private val pushContent = mutableMapOf<String, PushedTweet>()
    override val delayTime: Long = 5
    override val cycle: Long = delayTime

    @ExperimentalTime
    override fun retrieve() {
        if (TwitterApi.token.isNullOrEmpty()) {
            TwitterApi.getBearerToken()
        }

        val subList = HashSet<String>()
        BotVariables.perGroup.forEach {
            subList.addAll(it.twitterSubscribers)
        }

        subList.forEach {
            try {
                val tweet = TwitterApi.getTweetWithCache(it)
                val historyTweet = pushContent[it]?.tweet

                if (tweet != null && (historyTweet == null || !historyTweet.contentEquals(tweet))) {
                    pushContent[it] = PushedTweet(tweet, false)
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
        val pushQueue = HashMap<String, List<Long>>()
        BotVariables.perGroup.forEach {
            for (subs in it.twitterSubscribers) {
                if (pushQueue.containsKey(subs)) {
                    pushQueue[subs] = pushQueue[subs]?.plus(it.id) ?: continue
                }
            }
        }

        pushQueue.forEach { (tName, pushGroups) ->
            run {
                val tweet = pushContent[tName]?.tweet
                if (tweet != null) {
                    pushGroups.forEach {
                        if (GroupConfigManager.getConfigSafely(it).twitterPushEnabled) {
                            GlobalScope.launch {
                                var message = "${tweet.user.name} ($it) 发送了一条推文\n${tweet.getFullText()}".toMsgChain()
                                val group = BotVariables.bot.getGroup(it)
                                val image = tweet.getPictureOrNull(group)
                                if (image != null) {
                                    message += image
                                }
                                group.sendMessage(message)
                                delay(2_500)
                            }
                        }
                    }
                }

                pushContent[tName]?.isPushed = true
            }
        }
    }

    data class PushedTweet(val tweet: Tweet, var isPushed: Boolean)
}