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
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.comet.utils.time.hmsPattern
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
enum class IssueStateReason(val display: String) {
    @SerialName("completed")
    COMPLETED("已完成"),

    @SerialName("not_planned")
    NOT_PLANNED("未计划")
}

@Serializable
data class IssueEventData(
    /**
     * Issue 动作, 可以是 opened, edited, deleted, pinned, unpinned, closed, reopened, assigned, unassigned, labeled, unlabeled, locked, unlocked, transferred, milestoned, 或 demilestoned.
     */
    val action: String,
    val issue: IssueObject,
    val repository: RepoInfo,
    val sender: SenderInfo
) : GitHubEventData {

    @Serializable
    data class IssueObject(
        @SerialName("html_url")
        val url: String,
        val id: Long,
        val title: String,
        val number: Int,
        val state: String,
        val locked: Boolean,
        @SerialName("created_at")
        val createdTime: String,
        val body: String?,
        val user: UserObject,
        @SerialName("state_reason")
        val stateReason: IssueStateReason? = null
    ) {
        @Serializable
        data class UserObject(
            val login: String
        )

        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of("UTC"))
            return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) + " " + hmsPattern.format(
                localTime
            )
        }
    }

    @Serializable
    data class SenderInfo(
        val login: String,
        val id: Long
    )

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
        val repoUrl: String,
        @SerialName("updated_at")
        val updateTime: String,
        @SerialName("pushed_at")
        val pushTime: String
    )

    override fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        when (action) {
            "opened" -> {
                wrapper.appendTextln("\uD83D\uDC1B ${repository.fullName} 有新议题 #${issue.number}")
                wrapper.appendText("by ${issue.user.login} | ${issue.convertCreatedTime()}\n\n")
                wrapper.appendTextln(issue.title)
                wrapper.appendText("${issue.body?.limit(50)?.trim() ?: "没有描述"}\n\n")
                wrapper.appendText("查看全部 > ${issue.url}")
            }

            "closed" -> {
                wrapper.appendTextln("\uD83D\uDC1B ${repository.fullName} 议题 #${issue.number} 关闭")
                wrapper.appendTextln("by ${issue.user.login}")
                if (issue.stateReason != null) {
                    wrapper.appendText("关闭理由为 ${issue.stateReason.display}")
                }
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

    override fun type(): String = "issues"

    override fun url(): String = issue.url
}
