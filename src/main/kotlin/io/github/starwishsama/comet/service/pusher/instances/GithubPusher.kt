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
import io.github.starwishsama.comet.service.command.GitHubService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object GithubPusher {
    fun push(event: GithubEvent) {
        if (!event.sendable()) {
            return
        }

        val authorAndRepo = event.repoName()

        val consumer = GitHubService.repos.repos.filter {
            it.repoAuthor == authorAndRepo[0].toString() && (it.repoName == "*" || it.repoName == authorAndRepo[1].toString())
        }

        runBlocking {
            consumer.forEach {
                it.repoTarget.forEach { id ->
                    BotVariables.comet.getBot().getGroup(id)?.also { g ->
                        g.sendMessage(
                            event.toMessageWrapper().toMessageChain(g)
                        )
                    }
                    delay(RandomUtil.randomLong(10, 400))
                }
            }
        }

        BotVariables.netLogger.log(HinaLogLevel.Debug, "推送 WebHook 消息成功", prefix = "WebHook")
    }
}
