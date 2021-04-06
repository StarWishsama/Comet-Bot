package io.github.starwishsama.comet.api.thirdparty.github.data.api

import com.fasterxml.jackson.annotation.JsonProperty

data class RepoInfo(
    val id: Int,
    @JsonProperty("node_id")
    val nodeID: String,
    @JsonProperty("name")
    val repoName: String,
    @JsonProperty("full_name")
    val repoFullName: String,
    @JsonProperty("private")
    val private: Boolean,
    @JsonProperty("owner")
    val owner: OwnerInfo,
    @JsonProperty("html_url")
    val url: String,
    @JsonProperty("description")
    val description: String?
) {
    data class OwnerInfo(
        val login: String,
        val id: Int,
        @JsonProperty("html_url")
        val pageUrl: String
    )
}