/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.service.pusher.pushers

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.github.data.events.GithubEvent
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.service.command.GitHubService
import kotlinx.coroutines.runBlocking

object GithubPusher {
    fun push(event: GithubEvent) {
        if (!event.isSendableEvent()) {
            return
        }

        val authorAndRepo = event.repoName().split("/")

        val consumer = GitHubService.repos.repos.filter {
            it.repoAuthor == authorAndRepo[0] && (it.repoName == "*" || it.repoName == authorAndRepo[1])
        }

        runBlocking {
            consumer.forEach {
                it.repoTarget.forEach { id ->
                    CometVariables.comet.getBot().getGroup(id)?.also { g ->
                        event.toMessageWrapper().let { mw ->
                            if (mw.isUsable()) {
                                g.sendMessage(mw.toMessageChain(g))
                            }
                        }
                    }
                }
            }
        }

        CometVariables.netLogger.log(HinaLogLevel.Debug, "推送 WebHook 消息成功", prefix = "WebHook")
    }
}