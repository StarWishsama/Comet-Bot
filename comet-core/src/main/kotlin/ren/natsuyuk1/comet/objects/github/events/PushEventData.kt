/*
 * Copyright (c) 2019-2022 StarWishsama.
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
import ren.natsuyuk1.comet.service.refsPattern
import ren.natsuyuk1.comet.utils.time.hmsPattern
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Serializable
data class PushEventData(
    val ref: String,
    val before: String,
    val after: String,
    @SerialName("repository")
    val repoInfo: RepoInfo,
    @SerialName("pusher")
    val pusher: PusherInfo,
    val compare: String,
    @SerialName("commits")
    val commitInfo: List<CommitInfo>,
    @SerialName("head_commit")
    val headCommitInfo: CommitInfo?
) : GitHubEventData {
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
        @SerialName("pushed_at")
        val pushTime: Long
    )

    @Serializable
    data class PusherInfo(
        val name: String,
        // 推送者为 bot 时会为空
        val email: String?
    )

    @Serializable
    data class CommitInfo(
        val id: String,
        val message: String,
        val timestamp: String,
        val url: String,
        val committer: PusherInfo
    )

    private fun getLocalTime(time: Long): String {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) + " " + hmsPattern.format(
            Instant.ofEpochMilli(time * 1000L).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }

    fun getPushTimeAsString() = getLocalTime(repoInfo.pushTime)

    internal fun buildCommitList(): String {
        val reversed = commitInfo.reversed()

        return buildString {
            reversed.subList(0, commitInfo.size.coerceAtMost(10)).forEach {
                append("🔨 (${it.id.substring(0, 7)}) ${it.message.substringBefore("\n")} - ${it.committer.name}\n")
            }

            if (reversed.size > 10) {
                append("...等 ${commitInfo.size} 个提交\n")
            }
        }
    }

    override fun toMessageWrapper(): MessageWrapper {
        if (headCommitInfo == null || commitInfo.isEmpty()) {
            return MessageWrapper().setUsable(false)
        }

        return buildMessageWrapper {
            appendText("⬆️ 新提交 ${repoInfo.fullName} [${ref.replace(refsPattern, "")}]\n")
            appendText(
                "by ${headCommitInfo.committer.name} | ${getLocalTime(repoInfo.pushTime)}\n\n"
            )
            appendText(buildCommitList(), true)
            appendText("查看差异 > $compare")
        }
    }

    override fun repoName(): String {
        return repoInfo.fullName
    }

    override fun branchName(): String {
        return ref.replace(refsPattern, "")
    }

    override fun type(): String = "push"
    override fun url(): String = compare

    override fun isSendableEvent(): Boolean = true
}
