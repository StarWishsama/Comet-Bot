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

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.github.data.events.GithubEvent
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.stream.Collectors

object GithubPusher {
    fun push(event: GithubEvent) {
        if (!event.sendable()) {
            return
        }

        val consumer = GroupConfigManager.getAllConfigs().parallelStream().filter { cfg ->
            cfg.githubRepoSubscribers.any { it.split("/")[1] == "*" || it == event.repoName() }
        }.collect(Collectors.toList())

        runBlocking {
            consumer.forEach {
                BotVariables.comet.getBot().getGroup(it.id)?.also { g ->
                    g.sendMessage(
                        event.toMessageWrapper().toMessageChain(g)
                    )
                }
                delay(RandomUtil.randomLong(10, 400))
            }
        }

        BotVariables.netLogger.log(HinaLogLevel.Debug, "推送 WebHook 消息成功", prefix = "WebHook")
    }
}