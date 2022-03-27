/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.config

import cn.hutool.core.net.URLDecoder
import io.github.starwishsama.comet.api.thirdparty.github.GithubEventHandler
import io.github.starwishsama.comet.api.thirdparty.github.data.events.GithubEvent
import io.github.starwishsama.comet.service.server.ServerUtil
import kotlinx.serialization.Serializable

@Serializable
data class GithubRepos(
    val repos: MutableSet<GithubRepo> = mutableSetOf()
) {
    @Serializable
    data class GithubRepo(
        val repoAuthor: String,
        val repoName: String,
        val repoSecret: String,
        val repoTarget: MutableSet<Long>,
        val branchFilter: MutableSet<String> = mutableSetOf(),
    ) {
        fun getFullName(): String {
            return "${repoAuthor}/${repoName}"
        }

        override fun toString(): String {
            return getFullName()
        }
    }

    fun isEmpty(): Boolean {
        return repos.isEmpty()
    }

    fun checkSecret(secret: String?, requestBody: String, eventType: String): SecretStatus {
        val parse: GithubEvent =
            GithubEventHandler.process(
                URLDecoder.decode(requestBody.replace("payload=", ""), Charsets.UTF_8),
                eventType
            )
                ?: return SecretStatus.FAILED

        val targetRepo = this.repos.find { it.getFullName() == parse.repoName() } ?: return SecretStatus.NOT_FOUND

        if (targetRepo.repoSecret.isEmpty() && secret == null) {
            return SecretStatus.NO_SECRET
        }

        val checkStatus = ServerUtil.checkSignature(targetRepo.repoSecret, secret ?: "", requestBody)

        return if (checkStatus) {
            SecretStatus.HAS_SECRET
        } else {
            SecretStatus.UNAUTHORIZED
        }
    }

    fun contains(repoAuthor: String, repoName: String, groupId: Long): GithubRepo? {
        val targetRepos = this.repos.filter { it.repoAuthor == repoAuthor }
        val targetRepo = targetRepos.find { it.repoName == repoName }
        return targetRepos.find { it.repoName == "*" && it.repoTarget.contains(groupId) } ?: targetRepo
    }

    fun add(target: Long, repoAuthor: String, repoName: String, repoSecret: String = ""): Boolean {
        val targetRepo = this.repos.find { it.repoAuthor == repoAuthor && it.repoName == repoName }
        if (targetRepo == null) {
            this.repos.add(
                GithubRepo(
                    repoAuthor,
                    repoName,
                    repoSecret,
                    mutableSetOf(target)
                )
            )

            return true
        } else {
            if (!targetRepo.repoTarget.contains(target)) {
                targetRepo.repoTarget.add(target)
                return true
            }
        }

        return false
    }

    fun remove(from: Long, repoAuthor: String, repoName: String): Boolean {
        if (repoName == "*") {
            val targetRepos = this.repos.filter { it.repoAuthor == repoAuthor && it.repoTarget.contains(from) }

            targetRepos.forEach {
                repos.remove(it)
            }

            return true
        } else {
            val targetRepo =
                this.repos.find { it.repoAuthor == repoAuthor && it.repoName == repoName && it.repoTarget.contains(from) }
                    ?: return false

            return repos.remove(targetRepo)
        }
    }
}

enum class SecretStatus {

    /**
     * 找不到任何使用 Secret 的项目, 可视作成功
     */
    NO_SECRET,

    /**
     * 已找到对应 Secret 的项目, 可视作成功
     */
    HAS_SECRET,

    /**
     * 已找到对应 Secret 的项目, 但无法匹配
     */
    UNAUTHORIZED,

    /**
     * 找不到请求的项目
     */
    NOT_FOUND,

    /**
     * 因其他原因验证失败
     */
    FAILED
}

