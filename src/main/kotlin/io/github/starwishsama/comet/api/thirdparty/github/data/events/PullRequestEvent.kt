/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.github.data.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

data class PullRequestEvent(
    val action: String,
    @JsonProperty("pull_request")
    val pullRequestInfo: PullRequestInfo,
    @JsonProperty("repository")
    val repository: RepoInfo,
    val sender: IssueEvent.SenderInfo
) : GithubEvent {

    data class RepoInfo(
        val id: Long,
        @JsonProperty("node_id")
        val nodeID: String,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("full_name")
        val fullName: String,
        @JsonProperty("private")
        val isPrivate: Boolean,
        @JsonProperty("owner")
        val owner: JsonNode,
        @JsonProperty("html_url")
        val repoUrl: String,
    )

    data class PullRequestInfo(
        @JsonProperty("html_url")
        val url: String,
        val title: String,
        val body: String,
        @JsonProperty("created_at")
        val createdTime: String,
    ) {
        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of("UTC"))
            return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) + " " + CometVariables.hmsPattern.format(
                localTime
            )
        }
    }

    override fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText("\uD83D\uDD27 新提交更改 ${repository.fullName}\n")
        wrapper.addText("by ${sender.login} | ${pullRequestInfo.convertCreatedTime()}\n\n")
        wrapper.addText("${pullRequestInfo.title}\n")
        wrapper.addText("${pullRequestInfo.body.limitStringSize(100).trim()}\n\n")
        wrapper.addText("查看全部 > ${pullRequestInfo.url}")

        return wrapper
    }

    override fun repoName(): String = repository.fullName

    override fun branchName(): String = ""

    override fun isSendableEvent(): Boolean = action == "opened"
}
