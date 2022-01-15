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
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.convertToDynamicData
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.CometPusherData
import io.github.starwishsama.comet.service.pusher.context.BiliBiliDynamicContext

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class BiliBiliDynamicPusher : CometPusher("bili_dynamic", CometPusherData(3, TimeUnit.MINUTES)) {
    override fun retrieve() {
        GroupConfigManager.getAllConfigs().parallelStream().forEach { config ->
            if (!config.biliPushEnabled) {
                return@forEach
            }

            config.biliSubscribers.forEach user@{ user ->
                val cache =
                    data.cache.find { (it as BiliBiliDynamicContext).pushUser.id == user.id } as BiliBiliDynamicContext?

                val dynamic: Dynamic = try {
                    DynamicApi.getUserDynamicTimeline(user.id.toLong())
                } catch (e: RuntimeException) {
                    if (e !is ApiException) {
                        CometVariables.daemonLogger.warning("在获取动态时出现了异常", e)
                    }
                    null
                } ?: return@user

                val sentTime = dynamic.convertToDynamicData()?.getSentTime() ?: return@user

                // Avoid too outdated dynamic
                if (sentTime.plusDays(1).isBefore(LocalDateTime.now())) {
                    return@user
                }

                val time = System.currentTimeMillis()

                val current = BiliBiliDynamicContext(
                    mutableSetOf(config.id),
                    time,
                    pushUser = user,
                    dynamicId = dynamic.getDynamicID()
                )

                if (cache == null) {
                    data.cache.add(current)
                } else if (!cache.contentEquals(current)) {
                    data.cache.remove(cache)
                    data.cache.add(current.also {
                        it.addPushTargets(cache.pushTarget)
                    })
                }
            }
        }

    }
}