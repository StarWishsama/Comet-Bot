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
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class IssueEvent(
    /**
     * Issue 动作, 可以是 opened, edited, deleted, pinned, unpinned, closed, reopened, assigned, unassigned, labeled, unlabeled, locked, unlocked, transferred, milestoned, 或 demilestoned.
     */
    val action: String,
    val issue: IssueObject,
    val repository: RepoInfo,
    val sender: SenderInfo
) : GithubEvent {

    data class IssueObject(
        @JsonProperty("html_url")
        val url: String,
        val id: Long,
        val title: String,
        val number: Int,
        val state: String,
        val locked: Boolean,
        @JsonProperty("created_at")
        val createdTime: String,
        val body: String,
    ) {
        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.systemDefault())
            return BotVariables.yyMMddPattern.format(localTime)
        }
    }

    data class SenderInfo(
        val login: String,
        val id: Long
    )

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
        @JsonProperty("updated_at")
        val updateTime: String,
        @JsonProperty("pushed_at")
        val pushTime: String,
    )

    override fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText("| 仓库 ${repository.fullName} 有新议题啦\n")
        wrapper.addText("| 议题 #${issue.number}\n")
        wrapper.addText("| 创建时间 ${issue.convertCreatedTime()}\n")
        wrapper.addText("| 创建人 ${sender.login}\n")
        wrapper.addText("| 查看详细信息: ${issue.url}\n")
        wrapper.addText("| 简略信息: \n")
        wrapper.addText("| ${issue.title}\n")
        wrapper.addText("| ${issue.body.limitStringSize(30)}\n")

        return wrapper
    }

    override fun repoName(): String {
        return repository.fullName
    }

    override fun sendable(): Boolean {
        return action == "opened"
    }
}