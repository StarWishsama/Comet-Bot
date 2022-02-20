/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.pushers

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.LiveApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.live.toSimpleLiveRoomData
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.CometPusherData
import io.github.starwishsama.comet.service.pusher.context.BiliBiliLiveContext
import io.github.starwishsama.comet.utils.noCatchCancellation
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class BiliBiliLivePusher : CometPusher("bili_live", CometPusherData(5, TimeUnit.MINUTES)) {
    override fun retrieve() {
        GroupConfigManager.getAllConfigs().forEach cfg@{ cfg ->
            if (cfg.biliPushEnabled) {
                cfg.biliSubscribers.forEach user@{ user ->
                    kotlin.runCatching {
                        if (user.roomID < 1L) {
                            return@user
                        }

                        val cache =
                            data.cache.find { (it as BiliBiliLiveContext).pushUser.id == user.id } as BiliBiliLiveContext?

                        val liveRoomInfo = runBlocking { LiveApi.getLiveInfo(user.roomID) } ?: return@user
                        val time = System.currentTimeMillis()
                        val current = BiliBiliLiveContext(
                            mutableSetOf(cfg.id),
                            time,
                            pushUser = user,
                            liveRoomInfo = liveRoomInfo.toSimpleLiveRoomData()
                        )

                        if (cache == null) {
                            data.cache.add(current)
                        } else if (!cache.contentEquals(current)) {
                            data.cache.remove(cache)
                            data.cache.add(current.also { it.addPushTargets(cache.pushTarget) })
                        } else {
                            cache.addPushTarget(cfg.id)
                        }

                    }.noCatchCancellation {
                        CometVariables.logger.error("获取 ${user.id} 直播间失败", it)
                    }
                }
            }
        }
    }
}