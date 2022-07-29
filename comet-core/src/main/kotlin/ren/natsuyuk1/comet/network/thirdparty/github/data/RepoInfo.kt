package ren.natsuyuk1.comet.network.thirdparty.github.data

import kotlinx.serialization.SerialName

data class RepoInfo(
    val id: Int,
    @SerialName("node_id")
    val nodeID: String,
    @SerialName("name")
    val repoName: String,
    @SerialName("full_name")
    val repoFullName: String,
    @SerialName("private")
    val private: Boolean,
    @SerialName("owner")
    val owner: OwnerInfo,
    @SerialName("html_url")
    val url: String,
    @SerialName("description")
    val description: String?
) {
    data class OwnerInfo(
        val login: String,
        val id: Int,
        @SerialName("html_url")
        val pageUrl: String
    )
}
