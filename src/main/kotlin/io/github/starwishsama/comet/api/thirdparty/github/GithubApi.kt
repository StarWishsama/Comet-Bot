/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.github

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.github.data.api.RepoInfo
import io.github.starwishsama.comet.api.thirdparty.github.data.api.UserInfo
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.network.NetUtil
import org.jsoup.Jsoup

object GithubApi {
    private const val apiUrl = "https://api.github.com"
    private val repoCache = mutableMapOf<String, String>()
    private val userCache = mutableSetOf<String>()

    fun getRepoInfo(owner: String, repoName: String): RepoInfo? {
        val response = NetUtil.getPageContent("${apiUrl}/repos/${owner}/${repoName}") ?: return null

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
        return if (repoCache[author] == repo) {
            true
        } else {
            return if (getRepoInfo(author, repo) != null) {
                repoCache.putIfAbsent(author, repo)
                true
            } else {
                false
            }
        }
    }

    fun getRepoInfoPicture(author: String, repo: String): MessageWrapper {
        if (isRepoExists(author, repo)) {
            val conn = Jsoup.connect("https://github.com/$author/$repo")

            conn.header("user-agent", NetUtil.defaultUA).followRedirects(true)
                .apply {
                    if (CometVariables.cfg.proxySwitch) {
                        proxy(CometVariables.cfg.proxyUrl, CometVariables.cfg.proxyPort)
                    }
                }

            conn.timeout(5_000)

            val doc = conn.get()
            val image = doc.select("meta[property=og:image]")

            return if (image.isNotEmpty()) {
                MessageWrapper().addPictureByURL(image[0].attr("content"))
            } else {
                MessageWrapper().addText("找不到指定的仓库")
            }

        } else {
            return MessageWrapper().addText("找不到指定的仓库")
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