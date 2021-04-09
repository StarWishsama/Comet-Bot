package io.github.starwishsama.comet.api.thirdparty.github.data.events

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.github.data.api.RepoInfo
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class PingEvent(
    val zen: String,
    @JsonProperty("hook_id")
    val hookID: Long,
    @JsonProperty("hook")
    val hookInfo: HookInfo,
    @JsonProperty("repository")
    val repositoryInfo: RepoInfo
) : GithubEvent {
    data class HookInfo(
        val type: String,
        val id: Long,
        val name: String,
        val active: Boolean,
        val events: List<String>
    )

    // Ping 事件无需转换
    override fun toMessageWrapper(): MessageWrapper {
        return MessageWrapper().setUsable(false)
    }

    override fun repoName(): String {
        return repositoryInfo.repoFullName
    }

    override fun sendable(): Boolean = false
}