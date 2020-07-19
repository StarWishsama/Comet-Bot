package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.utils.toMirai
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TweetUpdateChecker : Runnable {
    private val pushedMap = mutableMapOf<String, Tweet>()

    override fun run() {
        if (TwitterApi.token.isNullOrEmpty()) {
            TwitterApi.getBearerToken()
        }

        BotVariables.cfg.twitterSubs.forEach {
            try {
                val tweet = TwitterApi.getTweetWithCache(it)
                val historyTweet = pushedMap[it]

                if (tweet != null && (historyTweet == null || !historyTweet.contentEquals(tweet))) {
                    pushedMap[it] = tweet
                    TwitterApi.addCacheTweet(it, tweet)

                    Comet.bot.groups.forEach { group ->
                        if (BotVariables.cfg.tweetPushGroups.contains(group.id)) {
                            GlobalScope.launch {
                                var message = "${tweet.user.name} ($it) 发送了一条推文\n${tweet.getFullText()}".toMirai()
                                val image = tweet.getPictureOrNull(group)
                                if (image != null) {
                                    message += image
                                }
                                group.sendMessage(message)
                                delay(2000)
                            }
                        }
                    }
                }
            } catch (e: RateLimitException) {
                Comet.logger.debug(e.localizedMessage)
            }
        }
    }
}