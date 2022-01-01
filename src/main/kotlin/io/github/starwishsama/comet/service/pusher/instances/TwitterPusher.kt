/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.instances

import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import io.github.starwishsama.comet.service.pusher.context.TwitterContext
import java.util.concurrent.TimeUnit

class TwitterPusher(comet: Comet) : CometPusher(comet, "twitter", PusherConfig(5, TimeUnit.MINUTES)) {
    override fun retrieve() {
        GroupConfigManager.getAllConfigs().parallelStream().forEach { cfg ->
            if (cfg.twitterPushEnabled) {
                cfg.twitterSubscribers.forEach tweet@{ user ->
                    val cache = cachePool.find { (it as TwitterContext).twitterUserName == user } as TwitterContext?

                    val latestTweet = TwitterApi.getTweetInTimeline(user, max = 1) ?: return@tweet
                    val time = System.currentTimeMillis()

                    val current = TwitterContext(
                        retrieveTime = time,
                        status = PushStatus.CREATED,
                        twitterUserName = user,
                        tweetId = latestTweet.id
                    )

                    if (cache == null) {
                        cachePool.add(current.also { it.addPushTarget(cfg.id) })
                        retrieveTime++
                    } else if (!cache.contentEquals(current)) {
                        cachePool.remove(cache)

                        current.apply {
                            addPushTargets(cache.getPushTarget())
                            cachePool.add(this)
                        }
                        retrieveTime++
                    }
                }
            }
        }

        CometVariables.daemonLogger.verbose("已获取了 $retrieveTime 条推文")
        retrieveTime = 0
    }
}
