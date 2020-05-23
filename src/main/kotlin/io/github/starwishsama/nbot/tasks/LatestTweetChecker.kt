package io.github.starwishsama.nbot.tasks

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.api.twitter.TwitterApi
import io.github.starwishsama.nbot.objects.pojo.twitter.Tweet
import io.github.starwishsama.nbot.util.toMirai
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object LatestTweetChecker : Runnable {
    private val pushedMap = mutableMapOf<String, Tweet>()

    override fun run() {
        BotConstants.cfg.twitterSubs.forEach {
            val tweet = TwitterApi.getLatestTweet(it)
            val historyTweet = pushedMap[it]
            val name = TwitterApi.getUserInfo(it)?.name
            if (tweet != null) {
                if (historyTweet != null && historyTweet.contentEquals(tweet)) {
                    return
                }

                pushedMap[it] = tweet

                BotMain.bot.groups.forEach { group ->
                    if (BotConstants.cfg.tweetPushGroups.contains(group.id)) {
                        runBlocking {
                            var message = "$name ($it) 发送了一条推文\n${tweet.text}".toMirai()
                            val image = tweet.getPictureOrNull(group.botAsMember)
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