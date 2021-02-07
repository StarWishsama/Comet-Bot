package io.github.starwishsama.comet.service.pusher.instances

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.BiliBiliMainApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.*
import net.mamoe.mirai.Bot

@Suppress("UNCHECKED_CAST")
class BiliDynamicPusher(bot: Bot) : CometPusher(bot, "bili_dynamic") {
    override var config: PusherConfig = PusherConfig(
        30_0000L,
        mutableListOf()
    )

    override val cachePool: MutableList<PushContext> = mutableListOf()

    override fun retrieve() {
        GroupConfigManager.getAllConfigs().forEach { cfg ->
            if (cfg.biliPushEnabled) {
                cfg.biliSubscribers.forEach user@{ user ->
                    val dynamic: Dynamic = try {
                        BiliBiliMainApi.getUserDynamicTimeline(user.id.toLong())
                    } catch (e: RuntimeException) {
                        if (e !is ApiException) {
                            BotVariables.daemonLogger.warning("在获取动态时出现了异常", e)
                        }
                        null
                    } ?: return@user
                    val time = System.currentTimeMillis()
                    val cache = cachePool.getDynamicContext(user.id.toLong())
                    val current = BiliBiliDynamicContext(
                        mutableListOf(cfg.id),
                        time,
                        pushUser = user,
                        dynamic = dynamic
                    )

                    if (cache == null) {
                        cachePool.add(current)
                        addRetrieveTime()
                        return@user
                    } else if (!cache.contentEquals(current)) {
                        cache.apply {
                            this.retrieveTime = time
                            this.dynamic = dynamic
                            this.status = PushStatus.READY
                            addPushTarget(cfg.id)
                        }

                        addRetrieveTime()
                    }
                }
            }
        }

        if (retrieveTime > 0) {
            BotVariables.daemonLogger.verbose("已获取了 $retrieveTime 个动态")
            resetRetrieveTime()
        }
    }
}