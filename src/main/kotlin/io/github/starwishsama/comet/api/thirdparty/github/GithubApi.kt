package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.github.data.api.RepoInfo
import io.github.starwishsama.comet.api.thirdparty.github.data.api.UserInfo
import io.github.starwishsama.comet.utils.network.NetUtil

object GithubApi {
    private const val apiUrl = "https://api.github.com"
    private val caches = mutableMapOf<String, String>()
    private val userCache = mutableSetOf<String>()

    fun getRepoInfo(owner: String, repoName: String): RepoInfo? {
        val response = NetUtil.getPageContent("${apiUrl}/${owner}/${repoName}") ?: return null

        return try {
            mapper.readValue<RepoInfo>(response)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserInfo(username: String): UserInfo? {
        val resp = NetUtil.getPageContent("${apiUrl}/users/${username}") ?: return null

        return try {
            mapper.readValue<UserInfo>(resp)
        } catch (e: Exception) {
            null
        }
    }

    fun isRepoExists(author: String, repo: String): Boolean {
        return if (caches[author] == repo) {
            true
        } else {
            return if (getRepoInfo(author, repo) != null) {
                caches.putIfAbsent(author, repo)
                true
            } else {
                false
            }
        }
    }

    fun isUserExists(username: String): Boolean {
        return if (userCache.contains(username)) {
            true
        } else {
            return if (getUserInfo(username) == null) {
                userCache.add(username)
                false
            } else {
                true
            }
        }
    }
}