package ren.natsuyuk1.comet.network.thirdparty.github.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubFileCommitInfo(
    @SerialName("node_id")
    val nodeId: String,
    val commit: CommitInfo,
    @SerialName("html_url")
    val url: String
) {
    @Serializable
    data class CommitInfo(
        val author: UserInfo,
        val committer: UserInfo,
        val message: String,
        val url: String
    ) {
        @Serializable
        data class UserInfo(
            val name: String,
            val email: String,
            val date: String
        )
    }
}
