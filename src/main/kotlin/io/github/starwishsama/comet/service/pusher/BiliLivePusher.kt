package io.github.starwishsama.comet.service.pusher

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.exceptions.RateLimitException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.BiliBiliLiveContext
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import io.github.starwishsama.comet.service.pusher.context.getContextByUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot

class BiliLivePusher(bot: Bot, config: PusherConfig): CometPusher(bot, "哔哩哔哩开播推送", config) {
    override val cachePool: MutableList<BiliBiliLiveContext> = mutableListOf()

    override fun retrieve() {
        try {
            checkReachLimit()
        } catch (e: RateLimitException) {
            daemonLogger.verbose(e.message)
            return
        }

        GroupConfigManager.getAllConfigs().forEach cfg@ { cfg ->
            if (!cfg.biliPushEnabled || cfg.biliSubscribers.isEmpty()) return@cfg

            cfg.biliSubscribers.forEach user@ { user ->
                val status = LiveApi.getLiveInfo(user.roomID) ?: return@user
                val cache = cachePool.getContextByUID(user.uid)
                val current = BiliBiliLiveContext(
                    mutableListOf(cfg.id),
                    System.currentTimeMillis(),
                    pushUser = user,
                    liveStatus = status
                )

                if (cache == null) {
                    cachePool.add(current)
                    return@user
                } else if (!cache.compareTo(current)){
                    cache.apply {
                        liveStatus = current.liveStatus
                        this.status = PushStatus.READY
                        addPushTarget(cfg.id)
                    }
                }
            }
        }
    }

    override fun push() {
        cachePool.forEach { context ->
            context.getPushTarget().forEach group@ {
                try {
                    val group = bot.getGroup(it) ?: return@group

                    runBlocking {
                        group.sendMessage(context.toMessageWrapper().toMessageChain(group))
                        delay(RandomUtil.randomLong(1000, 2000))
                    }
                } catch (e: Exception) {
                    daemonLogger.warning("在推送开播消息至群 $it 时出现异常", e)
                }
            }

            context.clearPushTarget()
            context.status = PushStatus.FINISHED
        }
    }
}