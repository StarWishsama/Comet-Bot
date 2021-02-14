package io.github.starwishsama.comet.service.pusher.instances

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.*
import net.mamoe.mirai.Bot

class TwitterPusher(bot: Bot): CometPusher(bot, "twitter") {
    override var config: PusherConfig = PusherConfig(
        30_0000L,
        mutableListOf()
    )

    override val cachePool: MutableList<PushContext> = mutableListOf()

    override fun retrieve() {
        GroupConfigManager.getAllConfigs().parallelStream().forEach { cfg ->
            if (cfg.twitterPushEnabled) {
                cfg.twitterSubscribers.forEach tweet@ { user ->
                    val tweet = TwitterApi.getTweetInTimeline(user) ?: return@tweet
                    val time = System.currentTimeMillis()
                    val cache = cachePool.getTwitterContext(user)

                    if (cache == null) {
                        cachePool.add(TwitterContext(mutableListOf(cfg.id), time, PushStatus.READY, user, tweet.id))
                        addRetrieveTime()
                    } else if (cache.tweetId != tweet.id) {
                        cache.apply {
                            this.retrieveTime = time
                            this.tweetId = tweet.id
                            this.status = PushStatus.READY
                        }
                        addRetrieveTime()
                    }
                }
            }
        }

        if (retrieveTime > 0) {
            BotVariables.daemonLogger.verbose("已获取了 $retrieveTime 条推文")
            resetRetrieveTime()
        }
    }
}