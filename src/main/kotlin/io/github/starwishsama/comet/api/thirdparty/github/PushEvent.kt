package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

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
    val commitInfo: List<CommitInfo>
) {
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
    )

    private fun getLocalTime(time: String): String {
        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val localTime = df.parse(time)
        return BotVariables.yyMMddPattern.format(localTime)
    }

    fun toMessageWrapper(): MessageWrapper {
        val wrapper = MessageWrapper()

        wrapper.addText("| ${repoInfo.fullName} 有推送提交了\n")
        wrapper.addText("| 推送时间 ${getLocalTime(repoInfo.updateTime)}\n")
        wrapper.addText("| 推送分支 ${ref.replace("refs/heads/", "")}")
        wrapper.addText("| 提交者 ${commitInfo[0].committer.name}\n")
        wrapper.addText("| 提交信息 \n")
        wrapper.addText("| ${commitInfo[0].message}\n")
        wrapper.addText("| 详细内容 > \n")
        wrapper.addText("| ${commitInfo[0].url}")

        return wrapper
    }
}