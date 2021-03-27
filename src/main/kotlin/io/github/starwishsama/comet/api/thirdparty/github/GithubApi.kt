package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.github.data.api.RepoInfo
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

object GithubApi {
    private const val apiUrl = "https://api.github.com"
    private val caches = mutableMapOf<String, String>()

    fun getRepoInfo(owner: String, repoName: String): RepoInfo? {
        val response = NetUtil.getPageContent("${apiUrl}/${owner}/${repoName}") ?: return null

        return try {
            mapper.readValue<RepoInfo>(response)
        } catch (e: Exception) {
            null
        }
    }

    fun isRepoExists(author: String, repo: String): Boolean {
        return if (caches[author] != repo) {
            getRepoInfo(author, repo) == null
        } else {
            true
        }
    }
}