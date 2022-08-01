package ren.natsuyuk1.comet.network.thirdparty.github

import cn.hutool.core.collection.ConcurrentHashSet
import io.ktor.client.request.*
import org.jsoup.Jsoup
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.github.data.RepoInfo
import ren.natsuyuk1.comet.network.thirdparty.github.data.UserInfo

object GitHubApi {
    private const val apiRoute = "https://api.github.com"
    private val repoCache = ConcurrentHashSet<String>()
    private val userCache = ConcurrentHashSet<String>()

    suspend fun getUserInfo(username: String): Result<UserInfo?> =
        runCatching<UserInfo?> {
            cometClient.client.get("${apiRoute}/users/${username}")
        }.onSuccess {
            userCache.add(username)
        }

    suspend fun isUserExist(username: String): Boolean =
        userCache.any { it == username } || getUserInfo(username).getOrNull() != null

    suspend fun getRepoInfo(owner: String, name: String): Result<RepoInfo?> =
        runCatching<RepoInfo?> {
            cometClient.client.get("$apiRoute/repos/$owner/$name")
        }.onSuccess {
            repoCache.add("$owner/$name")
        }

    suspend fun isRepoExist(owner: String, name: String): Boolean =
        repoCache.any { it == "$owner/$name" } || getRepoInfo(owner, name).getOrNull() != null

    suspend fun getRepoPreviewImage(owner: String, name: String): String? {
        if (!isRepoExist(owner, name)) {
            return null
        }

        val conn = Jsoup.connect("https://github.com/$owner/$name")

        conn.header("user-agent", CometConfig.data.useragent)
        conn.timeout(5_000)

        val doc = conn.get()
        return doc.select("meta[property=og:image]").firstOrNull()?.attr("content")
    }
}
