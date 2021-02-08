package io.github.starwishsama.comet.service.pusher.instances

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.BiliBiliLiveContext
import io.github.starwishsama.comet.service.pusher.context.PushContext
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import io.github.starwishsama.comet.service.pusher.context.getLiveContext
import io.github.starwishsama.comet.utils.debugS
import net.mamoe.mirai.Bot

class BiliLivePusher(bot: Bot): CometPusher(bot, "bili_live") {
    override var config: PusherConfig = PusherConfig(
        30_0000L,
        mutableListOf()
    )
    override val cachePool: MutableList<PushContext> = mutableListOf()

    override fun retrieve() {
        GroupConfigManager.getAllConfigs().forEach cfg@{ cfg ->
            if (cfg.biliPushEnabled) {
                cfg.biliSubscribers.forEach user@{ user ->
                    if (user.roomID < 0L) {
                        return@user
                    }

                    val liveRoomInfo = LiveApi.getLiveInfo(user.roomID) ?: return@user
                    val time = System.currentTimeMillis()
                    val cache = cachePool.getLiveContext(user.id.toLong())
                    val current = BiliBiliLiveContext(
                        mutableListOf(cfg.id),
                        time,
                        pushUser = user,
                        liveRoomInfo = liveRoomInfo
                    )

                    daemonLogger.debugS(liveRoomInfo.data.toString())
                    daemonLogger.debugS(cache?.liveRoomInfo?.data.toString())

                    if (cache == null) {
                        cachePool.add(current)
                        addRetrieveTime()
                    } else if (!cache.contentEquals(current)) {
                        cache.apply {
                            retrieveTime = time
                            this.liveRoomInfo = current.liveRoomInfo
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
}