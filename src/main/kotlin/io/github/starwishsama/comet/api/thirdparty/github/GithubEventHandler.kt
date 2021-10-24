/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.github.data.events.*
import io.github.starwishsama.comet.logger.HinaLogLevel

object GithubEventHandler {
    fun process(raw: String, type: String): GithubEvent? {
        return when (type) {
            "ping" -> {
                mapper.readValue<PingEvent>(raw)
            }
            "issues" -> {
                mapper.readValue<IssueEvent>(raw)
            }
            "push" -> {
                mapper.readValue<PushEvent>(raw)
            }
            "issue_comment" -> {
                mapper.readValue<IssueCommentEvent>(raw)
            }
            "release" -> {
                mapper.readValue<ReleaseEvent>(raw)
            }
            "pull_request" -> {
                mapper.readValue<PullRequestEvent>(raw)
            }
            else -> {
                CometVariables.netLogger.log(HinaLogLevel.Debug, "解析 WebHook 消息失败, 不支持的事件类型", prefix = "WebHook")
                null
            }
        }
    }
}
