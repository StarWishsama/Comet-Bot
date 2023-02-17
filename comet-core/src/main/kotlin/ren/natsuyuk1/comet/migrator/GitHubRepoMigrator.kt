package ren.natsuyuk1.comet.migrator

import mu.KotlinLogging
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.migrator.olddata.OldGitHubRepo
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import java.io.File

private val logger = KotlinLogging.logger {}

object GitHubRepoMigrator : IMigrator {
    override suspend fun migrate() {
        if (!oldFilePath.exists() || !oldFilePath.isDirectory) {
            return
        }

        val githubRepo = File(oldFilePath, "repos.yml")

        fun OldGitHubRepo.GithubRepo.migrateToSubscriber():
            MutableList<GitHubRepoData.Data.GithubRepo.GithubRepoSubscriber> {
            val result =
                mutableListOf<GitHubRepoData.Data.GithubRepo.GithubRepoSubscriber>()

            for (groupId in this.repoTarget) {
                result.add(
                    GitHubRepoData.Data.GithubRepo.GithubRepoSubscriber(
                        groupId,
                    ).also { it.subscribeBranch.addAll(this.branchFilter) },
                )
            }

            return result
        }

        if (githubRepo.exists()) {
            logger.info { "正在导入 GitHub 仓库订阅信息." }

            val oldRepos = Yaml.Default.decodeFromString(OldGitHubRepo.serializer(), githubRepo.readTextBuffered())
            val pendingRemove = mutableListOf<OldGitHubRepo.GithubRepo>()

            oldRepos.repos.forEach { repo ->
                val exists = GitHubRepoData.data.repos.find {
                    it.getName() == "${repo.repoAuthor}/${repo.repoName}" && it.subscribers.any { g ->
                        repo.repoTarget.contains(g.id)
                    }
                } != null

                if (!exists) {
                    GitHubRepoData.data.repos.add(
                        GitHubRepoData.Data.GithubRepo(
                            repo.repoName,
                            repo.repoAuthor,
                            repo.repoSecret,
                            repo.migrateToSubscriber(),
                        ),
                    )
                } else {
                    pendingRemove.add(repo)
                }
            }

            oldRepos.repos.removeIf {
                pendingRemove.contains(it)
            }

            logger.info { "成功导入 ${oldRepos.repos.size} 个 GitHub 仓库订阅信息." }

            githubRepo.delete()
        }
    }
}
