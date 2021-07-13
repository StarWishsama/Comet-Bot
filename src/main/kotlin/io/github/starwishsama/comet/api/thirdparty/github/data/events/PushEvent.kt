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

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PushEvent(
    val ref: String,
    val before: String,
    val after: String,
    @JsonProperty("repository")
    val repoInfo: RepoInfo,
    @JsonProperty("pusher")
    val pusher: PusherInfo,
    val compare: String,
    @JsonProperty("commits")
    val commitInfo: List<CommitInfo>,
    @JsonProperty("head_commit")
    val headCommitInfo: CommitInfo
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
        @JsonProperty("pushed_at")
        val pushTime: Long,
    )

    data class PusherInfo(
        val name: String,
        val email: String
    )

    data class CommitInfo(
        val id: String,
        val message: String,
        val timestamp: String,
        val url: String,
        val committer: PusherInfo
    ) {
        fun convertTimestamp(): String {
            val localTime =
                LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.systemDefault())
            return CometVariables.yyMMddPattern.format(localTime)
        }
    }

    private fun getLocalTime(time: Long): String {
        return CometVariables.yyMMddPattern.format(
            Instant.ofEpochMilli(time * 1000L).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    override fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText("⬆️ 仓库 ${repoInfo.fullName} 有新提交啦\n")
        wrapper.addText("| 推送时间 ${getLocalTime(repoInfo.pushTime)}\n")
        wrapper.addText("| 推送分支 ${ref.replace("refs/heads/", "")}\n")
        wrapper.addText("| 提交者 ${headCommitInfo.committer.name}\n")
        wrapper.addText("| 提交信息 \n")
        wrapper.addText("| ${headCommitInfo.message}\n")
        wrapper.addText("| 查看差异: \n")
        wrapper.addText(compare)

        return wrapper
    }

    override fun repoName(): String {
        return repoInfo.fullName
    }

    override fun sendable(): Boolean = true
}