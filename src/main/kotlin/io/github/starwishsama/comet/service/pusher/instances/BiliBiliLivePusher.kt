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
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.BiliBiliLiveContext
import java.util.concurrent.TimeUnit

class BiliBiliLivePusher(
    comet: Comet
) : CometPusher(comet, "bili_live", PusherConfig(5, TimeUnit.MINUTES)) {
    override fun retrieve() {
        GroupConfigManager.getAllConfigs().forEach cfg@{ cfg ->
            if (cfg.biliPushEnabled) {
                cfg.biliSubscribers.forEach user@{ user ->
                    if (user.roomID < 1L) {
                        return@user
                    }

                    val cache =
                        cachePool.find { (it as BiliBiliLiveContext).pushUser.id == user.id } as BiliBiliLiveContext?

                    val liveRoomInfo = LiveApi.getLiveInfo(user.roomID) ?: return@user
                    val time = System.currentTimeMillis()
                    val current = BiliBiliLiveContext(
                        mutableSetOf(cfg.id),
                        time,
                        pushUser = user,
                        liveRoomInfo = liveRoomInfo
                    )

                    if (cache == null) {
                        cachePool.add(current)
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

            retrieveTime = 0
        }

        CometVariables.daemonLogger.verbose("已获取了 $retrieveTime 个开播消息")
    }
}