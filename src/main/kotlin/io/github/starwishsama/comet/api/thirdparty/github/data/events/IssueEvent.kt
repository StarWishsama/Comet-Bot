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
        val body: String?,
        val user: UserObject,
    ) {
        data class UserObject(
            val login: String
        )

        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of("UTC"))
            return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) + " " + CometVariables.hmsPattern.format(
                localTime
            )
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

        when (action) {
            "opened" -> {
                wrapper.addText("\uD83D\uDC1B ${repository.fullName} 有新议题 #${issue.number}\n")
                wrapper.addText("by ${issue.user.login} | ${issue.convertCreatedTime()} \n\n")
                wrapper.addText("${issue.title}\n")
                wrapper.addText("${issue.body?.limitStringSize(50)?.trim()}\n\n")
                wrapper.addText("查看全部 >: ${issue.url}\n")
            }

            "closed" -> {
                wrapper.addText("\uD83D\uDC1B ${repository.fullName} 议题 #${issue.number} 关闭\n")
                wrapper.addText("由 ${issue.user.login} 创建\n")
                wrapper.addText("查看全部 > ${issue.url}\n")
            }
        }

        return wrapper
    }

    override fun repoName(): String {
        return repository.fullName
    }

    override fun branchName(): String {
        return ""
    }

    override fun isSendableEvent(): Boolean {
        return action == "opened" || action == "closed"
    }
}