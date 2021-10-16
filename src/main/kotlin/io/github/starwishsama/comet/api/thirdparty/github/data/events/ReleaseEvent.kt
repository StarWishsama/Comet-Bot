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

data class ReleaseEvent(
    // created, prereleased
    val action: String,
    val release: ReleaseInfo,
    val repository: IssueEvent.RepoInfo,
    val sender: IssueEvent.SenderInfo,
) : GithubEvent {

    data class ReleaseInfo(
        @JsonProperty("html_url")
        val url: String,
        @JsonProperty("tag_name")
        val tagName: String,
        @JsonProperty("name")
        val title: String,
        val body: String,
        @JsonProperty("created_at")
        val createdTime: String,
        @JsonProperty("published_at")
        val publishTime: String,
        val author: IssueEvent.SenderInfo
    ) {
        fun convertCreatedTime(): String {
            val localTime =
                LocalDateTime.parse(createdTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).atZone(ZoneId.of("UTC"))
            return CometVariables.hmsPattern.format(localTime)
        }
    }

    override fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText("\uD83D\uDCE6 ${repository.fullName} 有新版本发布\n")
        wrapper.addText("| 版本 #${release.tagName}\n")
        wrapper.addText("| 发布时间 ${release.convertCreatedTime()}\n")
        wrapper.addText("| 发布人 ${release.author.login}\n")
        wrapper.addText("| 发布版信息 \n")
        wrapper.addText("| ${release.title}\n")
        wrapper.addText("| ${release.body.limitStringSize(50).trim()}\n")
        wrapper.addText("| 查看完整信息: ${release.url}")

        return wrapper
    }

    override fun repoName(): String = repository.fullName

    override fun isSendableEvent(): Boolean = action == "released" || action == "prereleased"
}