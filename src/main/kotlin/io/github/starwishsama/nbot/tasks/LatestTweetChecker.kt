package io.github.starwishsama.nbot.tasks

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.api.twitter.TwitterApi
import io.github.starwishsama.nbot.objects.pojo.twitter.Tweet
import io.github.starwishsama.nbot.util.toMirai
import kotlinx.coroutines.*

object LatestTweetChecker : Runnable {
    private val pushedMap = mutableMapOf<String, Tweet>()

    @ObsoleteCoroutinesApi
    override fun run() {
        if (TwitterApi.token.isNullOrEmpty()) {
            TwitterApi.getBearerToken()
        }

        BotConstants.cfg.twitterSubs.forEach {
            val tweet = TwitterApi.getLatestTweet(it)
            val historyTweet = pushedMap[it]

            if (tweet != null && (historyTweet == null || !historyTweet.contentEquals(tweet))) {
                pushedMap[it] = tweet

                BotMain.bot.groups.forEach { group ->
                    if (BotConstants.cfg.tweetPushGroups.contains(group.id)) {
                        GlobalScope.launch(newSingleThreadContext("Push-Thread")) {
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
        }
    }
}