/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.instances

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.BiliBiliDynamicContext
import io.github.starwishsama.comet.service.pusher.context.PushStatus
import net.mamoe.mirai.Bot
import java.util.concurrent.TimeUnit

class BiliBiliDynamicPusher(
    bot: Bot
) : CometPusher(bot, "bili_dynamic", PusherConfig(3, TimeUnit.MINUTES)) {
    override fun retrieve() {
        GroupConfigManager.getAllConfigs().parallelStream().forEach { config ->
            if (!config.biliPushEnabled) {
                return@forEach
            }

            config.biliSubscribers.forEach user@{ user ->
                val target =
                    cachePool.find { (it as BiliBiliDynamicContext).pushUser.id == user.id } as BiliBiliDynamicContext?

                val dynamic: Dynamic = try {
                    DynamicApi.getUserDynamicTimeline(user.id.toLong())
                } catch (e: RuntimeException) {
                    if (e !is ApiException) {
                        BotVariables.daemonLogger.warning("在获取动态时出现了异常", e)
                    }
                    null
                } ?: return@user

                val time = System.currentTimeMillis()

                val current = BiliBiliDynamicContext(
                    mutableSetOf(config.id),
                    time,
                    pushUser = user,
                    dynamicId = dynamic.getDynamicID()
                )

                if (target == null) {
                    cachePool.add(
                        BiliBiliDynamicContext(
                            mutableSetOf(config.id),
                            time,
                            pushUser = user,
                            dynamicId = dynamic.getDynamicID()
                        )
                    )
                } else if (!target.contentEquals(current)) {
                    target.apply {
                        status = PushStatus.PROGRESSING
                        dynamicId = dynamic.getDynamicID()
                        retrieveTime = time
                    }

                    target.addPushTarget(config.id)
                }
            }
        }


        BotVariables.daemonLogger.verbose("已获取了 $retrieveTime 个动态")
        retrieveTime = 0

    }
}