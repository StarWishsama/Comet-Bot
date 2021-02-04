package io.github.starwishsama.comet.service.pusher.instances

import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.PushContext
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import io.github.starwishsama.comet.service.pusher.context.TwitterContext
import io.github.starwishsama.comet.service.pusher.context.getContext
import net.mamoe.mirai.Bot

@Suppress("UNCHECKED_CAST")
class TwitterPusher(bot: Bot): CometPusher(bot, "twitter") {
    override var config: PusherConfig = PusherConfig(
        30_0000L,
        90_0000L,
        mutableListOf()
    )

    override val cachePool: MutableList<PushContext> = mutableListOf()

    override fun retrieve() {
        GroupConfigManager.getAllConfigs().parallelStream().forEach { cfg ->
            if (cfg.twitterPushEnabled) {
                cfg.twitterSubscribers.forEach tweet@ { user ->
                    val tweet = TwitterApi.getTweetInTimeline(user) ?: return@tweet
                    val time = System.currentTimeMillis()
                    val cache = (cachePool as MutableList<TwitterContext>).getContext(user)

                    if (cache == null) {
                        cachePool.add(TwitterContext(mutableListOf(cfg.id), time, PushStatus.READY, user, tweet))
                        return@forEach
                    } else if (!tweet.contentEquals(cache.tweet)) {
                        cache.apply {
                            this.retrieveTime = time
                            this.tweet = tweet
                            this.status = PushStatus.READY
                        }
                    }
                }
            }
        }
    }
}