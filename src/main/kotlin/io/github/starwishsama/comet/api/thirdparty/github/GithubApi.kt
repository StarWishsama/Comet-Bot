package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.github.data.api.RepoInfo
import io.github.starwishsama.comet.utils.network.NetUtil

object GithubApi {
    private const val apiUrl = "https://api.github.com"

    fun getRepoInfo(owner: String, repoName: String): RepoInfo? {
        val response = NetUtil.getPageContent("${apiUrl}/${owner}/${repoName}") ?: return null

        return try {
            mapper.readValue<RepoInfo>(response)
        } catch (e: JsonParseException) {
            null
        }
    }
}