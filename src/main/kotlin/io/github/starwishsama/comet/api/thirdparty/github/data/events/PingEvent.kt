package io.github.starwishsama.comet.api.thirdparty.github.data.events

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.starwishsama.comet.api.thirdparty.github.data.api.RepoInfo

data class PingEvent(
    val zen: String,
    @JsonProperty("hook_id")
    val hookID: Long,
    @JsonProperty("hook")
    val hookInfo: HookInfo,
    @JsonProperty("repository")
    val repositoryInfo: RepoInfo
) {
    data class HookInfo(
        val type: String,
        val id: Long,
        val name: String,
        val active: Boolean,
        val events: List<String>
    )
}