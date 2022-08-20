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
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.comet.utils.time.hmsPattern
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class IssueCommentEventData(
    val action: String,
    val issue: IssueEventData.IssueObject,
    val comment: CommentObject,
    val repository: IssueEventData.RepoInfo
) : GithubEventData {
    @Serializable
    data class CommentObject(
        @SerialName("html_url")
        val url: String,
        val id: Long,
        val user: IssueEventData.SenderInfo,
        @SerialName("created_at")
        val createdTime: String,
        @SerialName("updated_at")
        val updatedTime: String,
        val body: String
    ) {
        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of("UTC"))
            return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) + " " + hmsPattern.format(
                localTime
            )
        }
    }

    override fun toMessageWrapper(): MessageWrapper {
        return MessageWrapper().apply {
            appendText("\uD83D\uDCAC ${repository.fullName} 议题 #${issue.number}\n")
            appendText("新回复 | ${comment.user.login} | ${comment.convertCreatedTime()}\n\n")
            appendText("${comment.body.limit(80).trim()}\n\n")
            appendText("查看全部 > ${comment.url}\n")
        }
    }

    override fun repoName(): String = repository.fullName

    override fun branchName(): String = ""

    override fun isSendableEvent(): Boolean = true

    override fun type(): String = "issue_comment"
}
