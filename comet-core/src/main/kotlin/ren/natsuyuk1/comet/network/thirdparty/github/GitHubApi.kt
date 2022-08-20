package ren.natsuyuk1.comet.network.thirdparty.github

import cn.hutool.core.collection.ConcurrentHashSet
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jsoup.Jsoup
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.network.thirdparty.github.data.GitHubFileCommitInfo
import ren.natsuyuk1.comet.network.thirdparty.github.data.RepoInfo
import ren.natsuyuk1.comet.network.thirdparty.github.data.UserInfo
import ren.natsuyuk1.comet.service.RateLimitAPI
import ren.natsuyuk1.comet.service.RateLimitService

object GitHubApi {
    private const val apiRoute = "https://api.github.com"
    private val repoCache = ConcurrentHashSet<String>()
    private val userCache = ConcurrentHashSet<String>()

    suspend fun getUserInfo(username: String): Result<UserInfo?> =
        runCatching<UserInfo?> {
            cometClient.client.get("$apiRoute/users/$username").body()
        }.onSuccess {
            userCache.add(username)
        }

    suspend fun isUserExist(username: String): Boolean =
        userCache.any { it == username } || getUserInfo(username).getOrNull() != null

    suspend fun getRepoInfo(owner: String, name: String): Result<RepoInfo> =
        runCatching<RepoInfo> {
            cometClient.client.get("$apiRoute/repos/$owner/$name").body()
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

        conn.header("user-agent", CometGlobalConfig.data.useragent)
        conn.timeout(5_000)

        val doc = conn.get()
        return doc.select("meta[property=og:image]").firstOrNull()?.attr("content")
    }

    suspend fun getSpecificFileCommits(
        owner: String,
        name: String,
        filePath: String,
        page: Int = 1,
        perPage: Int = 1
    ): Result<List<GitHubFileCommitInfo>> =
        runCatching<List<GitHubFileCommitInfo>> {
            if (RateLimitService.isRateLimit(RateLimitAPI.GITHUB)) {
                error("Reached GitHub rate limit")
            }

            val resp = cometClient.client.get("$apiRoute/repos/$owner/$name/commits") {
                parameter("path", filePath)
                parameter("page", page)
                parameter("per_page", perPage)
            }

            RateLimitService.checkRate(RateLimitAPI.GITHUB, resp.headers)

            if (resp.status != HttpStatusCode.OK) {
                emptyList()
            } else {
                resp.body()
            }
        }
}
