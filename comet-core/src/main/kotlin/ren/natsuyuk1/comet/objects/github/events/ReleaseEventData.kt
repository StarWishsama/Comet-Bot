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
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.comet.utils.time.hmsPattern
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class ReleaseEventData(
    // created, prereleased, deleted, published
    val action: String,
    val release: ReleaseInfo,
    val repository: IssueEventData.RepoInfo,
    val sender: IssueEventData.SenderInfo
) : GitHubEventData {

    @Serializable
    data class ReleaseInfo(
        @SerialName("html_url")
        val url: String,
        @SerialName("tag_name")
        val tagName: String,
        @SerialName("name")
        val title: String,
        val body: String,
        @SerialName("created_at")
        val createdTime: String,
        @SerialName("published_at")
        val publishTime: String,
        val author: IssueEventData.SenderInfo
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
            appendText("\uD83D\uDCE6 ${repository.fullName} ${release.tagName} 发布\n")
            appendText("| ${release.author.login} | ${release.convertCreatedTime()}\n")
            appendText("| \n")
            appendText("| ${release.title}\n")
            appendText("| ${release.body.limit(50).trim()}\n")
            appendText("| 查看详细信息: ${release.url}")
        }

    override fun repoName(): String = repository.fullName

    override fun branchName(): String {
        return ""
    }

    override fun type(): String = "release"
    override fun url(): String = release.url

    override fun isSendableEvent(): Boolean = action == "released" || action == "prereleased"
}
