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
        val repoTarget: MutableSet<Long>
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

    fun checkSecret(secret: List<String>?, requestBody: String, eventType: String): SecretStatus {
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

        val checkStatus = ServerUtil.checkSignature(targetRepo.repoSecret, secret?.get(0) ?: "", requestBody)

        return if (checkStatus) {
            SecretStatus.HAS_SECRET
        } else {
            SecretStatus.UNAUTHORIZED
        }
    }

    fun contains(repoAuthor: String, repoName: String): GithubRepo? {
        val targetRepos = this.repos.filter { it.repoAuthor == repoAuthor }
        val targetRepo = targetRepos.find { it.repoName == repoName }
        return targetRepos.find { it.repoName == "*" } ?: targetRepo
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
            val targetRepos = this.repos.filter { it.repoAuthor == repoAuthor }

            if (targetRepos.isNotEmpty()) {
                targetRepos.forEach {
                    it.repoTarget.remove(from)
                }
            }

            return targetRepos.none { it.repoTarget.contains(from) }
        } else {
            val targetRepo = this.repos.find { it.repoAuthor == repoAuthor && it.repoName == repoName } ?: return false

            val success = targetRepo.repoTarget.remove(from)

            return if (targetRepo.repoTarget.isEmpty()) {
                this.repos.remove(targetRepo)
            } else {
                success
            }
        }
    }
}

enum class SecretStatus {
    NO_SECRET, HAS_SECRET, UNAUTHORIZED, NOT_FOUND, FAILED
}

