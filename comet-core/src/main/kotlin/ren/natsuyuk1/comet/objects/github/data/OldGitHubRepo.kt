package ren.natsuyuk1.comet.objects.github.data

import kotlinx.serialization.Serializable

@Serializable
data class OldGitHubRepo(
    val repos: MutableSet<GithubRepo> = mutableSetOf()
) {
    @Serializable
    data class GithubRepo(
        val repoAuthor: String,
        val repoName: String,
        val repoSecret: String,
        val repoTarget: MutableSet<Long>,
        val branchFilter: MutableSet<String> = mutableSetOf(),
    ) {
        fun getFullName(): String {
            return "${repoAuthor}/${repoName}"
        }

        override fun toString(): String {
            return getFullName()
        }
    }

    fun isEmpty(): Boolean {
        return repos.isEmpty()
    }
}
