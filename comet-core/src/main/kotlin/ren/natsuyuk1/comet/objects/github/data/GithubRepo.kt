package ren.natsuyuk1.comet.objects.github.data

import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File

object GithubRepoData : PersistDataFile<GithubRepoData.Data>(
    File(configDirectory, "github_repos.json"),
    Data()
) {
    data class Data(
        val repos: MutableList<GithubRepo> = mutableListOf()
    ) {
        data class GithubRepo(
            val repoName: String,
            val owner: String,
            // 储存加密后的 Secret, 储存先必须先进行 String#toHMAC()
            val secret: String,
            val subscribers: List<GithubRepoSubscriber>
        ) {
            fun getName(): String = "$owner/$repoName"

            data class GithubRepoSubscriber(
                val id: Long,
                val subscribeEvent: List<String> = mutableListOf()
            )
        }
    }

    fun find(repoName: String) = data.repos.find { repoName == it.getName() }
}
