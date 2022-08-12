package ren.natsuyuk1.comet.objects.github.data

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object GithubRepoData : PersistDataFile<GithubRepoData.Data>(
    File(configDirectory, "github_repos.json"),
    Data()
) {
    @Serializable
    data class Data(
        val repos: MutableList<GithubRepo> = mutableListOf()
    ) {
        @Serializable
        data class GithubRepo(
            val repoName: String,
            val owner: String,
            val secret: String,
            val subscribers: MutableList<GithubRepoSubscriber>,
        ) {
            fun getName(): String = "$owner/$repoName"

            @Serializable
            data class GithubRepoSubscriber(
                val id: Long,
                val subscribeBranch: MutableSet<String> = mutableSetOf("master", "main"),
                val subscribeEvent: MutableSet<String> = mutableSetOf("push", "release", "issues", "issue_comment")
            )
        }
    }

    fun find(repoName: String) = data.repos.find { repoName == it.getName() }

    fun exists(repoName: String, groupId: Long) =
        data.repos.find { repoName == it.getName() && it.subscribers.any { sub -> sub.id == groupId } } != null
}
