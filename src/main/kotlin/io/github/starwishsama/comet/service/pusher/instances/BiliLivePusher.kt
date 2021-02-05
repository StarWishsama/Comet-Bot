package io.github.starwishsama.comet.service.pusher.instances

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.BiliBiliLiveContext
import io.github.starwishsama.comet.service.pusher.context.PushContext
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import io.github.starwishsama.comet.service.pusher.context.getContextByUID
import net.mamoe.mirai.Bot

@Suppress("UNCHECKED_CAST")
class BiliLivePusher(bot: Bot): CometPusher(bot, "bili_live") {
    override var config: PusherConfig = PusherConfig(
        30_0000L,
        mutableListOf()
    )
    override val cachePool: MutableList<PushContext> = mutableListOf()

    override fun retrieve() {
        GroupConfigManager.getAllConfigs().forEach cfg@ { cfg ->
            if (!cfg.biliPushEnabled || cfg.biliSubscribers.isEmpty()) return@cfg

            cfg.biliSubscribers.forEach user@ { user ->
                if (user.roomID < 0L) {
                    return@user
                }
                val status = LiveApi.getLiveInfo(user.roomID) ?: return@user
                val time = System.currentTimeMillis()
                val cache = (cachePool as MutableList<BiliBiliLiveContext>).getContextByUID(user.id.toLong())
                val current = BiliBiliLiveContext(
                    mutableListOf(cfg.id),
                    time,
                    pushUser = user,
                    liveStatus = status
                )

                if (cache == null) {
                    cachePool.add(current)
                    addRetrieveTime()
                    return@user
                } else if (!cache.compareTo(current)){
                    cache.apply {
                        retrieveTime = time
                        liveStatus = current.liveStatus
                        this.status = PushStatus.READY
                        addPushTarget(cfg.id)
                    }
                    addRetrieveTime()
                }
            }
        }

        if (retrieveTime > 0) {
            daemonLogger.verbose("已获取了 $retrieveTime 个开播消息")
            resetRetrieveTime()
        }
    }
}