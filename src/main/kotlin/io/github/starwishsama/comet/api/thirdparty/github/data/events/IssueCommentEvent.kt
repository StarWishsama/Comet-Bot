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
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class IssueCommentEvent(
    val action: String,
    val issue: IssueEvent.IssueObject,
    val comment: CommentObject,
    val repository: IssueEvent.RepoInfo,
) : GithubEvent {
    data class CommentObject(
        @JsonProperty("html_url")
        val url: String,
        val id: Long,
        val user: IssueEvent.SenderInfo,
        @JsonProperty("created_at")
        val createdTime: String,
        @JsonProperty("updated_at")
        val updatedTime: String,
        val body: String,
    ) {
        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.systemDefault())
            return CometVariables.hmsPattern.format(localTime)
        }
    }

    override fun toMessageWrapper(): MessageWrapper {
        return MessageWrapper().apply {
            addText("\uD83D\uDCAC ${repository.fullName} 议题 #${issue.number} 下有新回复\n")
            addText("| 创建时间 ${comment.convertCreatedTime()}\n")
            addText("| 创建人 ${comment.user.login}\n")
            addText("| 查看详细信息: ${comment.url}\n")
            addText("| 简略信息: \n")
            addText("| ${comment.body.limitStringSize(80).trim()}\n")

        }
    }

    override fun repoName(): String = repository.fullName

    override fun isSendableEvent(): Boolean = true
}