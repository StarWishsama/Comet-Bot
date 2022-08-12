package ren.natsuyuk1.comet.objects.github.data

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.utils.file.configDirectory
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveDirectory
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
                val subscribeEvent: MutableSet<String> = mutableSetOf("push", "release", "issue", "issue_comment")
            )
        }
    }

    override suspend fun init() {
        super.init()

        val oldFile = resolveDirectory("/old_comet")

        if (!oldFile.exists() || !oldFile.isDirectory) {
            return
        }

        val githubRepo = File(oldFile, "repos.json")

        fun OldGitHubRepo.GithubRepo.migrateToSubscriber(): MutableList<Data.GithubRepo.GithubRepoSubscriber> {
            val result = mutableListOf<Data.GithubRepo.GithubRepoSubscriber>()

            for (groupId in this.repoTarget) {
                result.add(
                    Data.GithubRepo.GithubRepoSubscriber(
                        groupId,
                        subscribeBranch = this.branchFilter
                    )
                )
            }

            return result
        }

        if (githubRepo.exists()) {
            val oldRepos = json.decodeFromString(OldGitHubRepo.serializer(), githubRepo.readTextBuffered())
            oldRepos.repos.forEach { repo ->
                GithubRepoData.data.repos.add(
                    Data.GithubRepo(
                        repo.repoName,
                        repo.repoAuthor,
                        repo.repoSecret,
                        repo.migrateToSubscriber()
                    )
                )
            }
        }
    }

    fun find(repoName: String) = data.repos.find { repoName == it.getName() }

    fun exists(repoName: String, groupId: Long) =
        data.repos.find { repoName == it.getName() && it.subscribers.any { sub -> sub.id == groupId } } != null
}
