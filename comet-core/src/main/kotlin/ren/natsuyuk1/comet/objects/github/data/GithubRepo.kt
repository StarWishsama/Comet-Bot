package ren.natsuyuk1.comet.objects.github.data

import kotlinx.serialization.Serializable
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.api.event.logger
import ren.natsuyuk1.comet.migrator.GitHubRepoMigrator
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
                val subscribeEvent: MutableSet<String> = mutableSetOf("push", "release", "issue", "issue_comment")
            )
        }
    }

    override suspend fun init() {
        super.init()

        try {
            GitHubRepoMigrator.migrate()
        } catch (e: Exception) {
            logger.warn(e) { "在迁移 GitHub 仓库数据时出现问题" }
        }
    }

    fun find(repoName: String) = data.repos.find { repoName == it.getName() }

    fun exists(repoName: String, groupId: Long) =
        data.repos.find { repoName == it.getName() && it.subscribers.any { sub -> sub.id == groupId } } != null
}
