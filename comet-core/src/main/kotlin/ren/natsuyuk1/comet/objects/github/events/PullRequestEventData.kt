/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.github.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.comet.utils.time.hmsPattern
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class PullRequestEventData(
    val action: String,
    @SerialName("pull_request")
    val pullRequestInfo: PullRequestInfo,
    @SerialName("repository")
    val repository: RepoInfo,
    val sender: IssueEventData.SenderInfo
) : GithubEventData {
    @Serializable
    data class RepoInfo(
        val id: Long,
        @SerialName("node_id")
        val nodeID: String,
        @SerialName("name")
        val name: String,
        @SerialName("full_name")
        val fullName: String,
        @SerialName("private")
        val isPrivate: Boolean,
        @SerialName("owner")
        val owner: JsonElement,
        @SerialName("html_url")
        val repoUrl: String
    )

    @Serializable
    data class PullRequestInfo(
        @SerialName("html_url")
        val url: String,
        val title: String,
        val body: String = "没有描述",
        @SerialName("created_at")
        val createdTime: String
    ) {
        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of("UTC"))
            return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) + " " + hmsPattern.format(
                localTime
            )
        }
    }

    override fun toMessageWrapper(): MessageWrapper =
        buildMessageWrapper {
            appendText("\uD83D\uDD27 新提交更改 ${repository.fullName}\n")
            appendText("by ${sender.login} | ${pullRequestInfo.convertCreatedTime()}\n\n")
            appendText("${pullRequestInfo.title}\n")
            appendText("${pullRequestInfo.body.limit(100).trim()}\n\n")
            appendText("查看全部 > ${pullRequestInfo.url}")
        }

    override fun repoName(): String = repository.fullName

    override fun branchName(): String = ""

    override fun type(): String = "pull_request"

    override fun isSendableEvent(): Boolean = action == "opened"
}
