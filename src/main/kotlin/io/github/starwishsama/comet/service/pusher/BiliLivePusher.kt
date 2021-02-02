package io.github.starwishsama.comet.service.pusher

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.service.pusher.config.PusherConfig

class BiliLivePusher(config: PusherConfig): CometPusher("哔哩哔哩开播推送", config) {
    override val cachePool: MutableList<PushContext<*>> = mutableListOf()

    override fun retrieve() {
        try {
            checkReachLimit()
        } catch (e: RateLimitException) {
            daemonLogger.verbose(e.message)
            return
        }


    }

    override fun push() {
        TODO("Not yet implemented")
    }
}