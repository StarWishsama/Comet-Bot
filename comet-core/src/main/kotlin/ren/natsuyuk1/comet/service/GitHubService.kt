package ren.natsuyuk1.comet.service

import cn.hutool.core.net.URLDecoder
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.registerListener
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.event.pusher.github.GithubEvent
import ren.natsuyuk1.comet.objects.github.data.GithubRepoData
import ren.natsuyuk1.comet.objects.github.data.SecretStatus
import ren.natsuyuk1.comet.objects.github.events.*
import ren.natsuyuk1.comet.utils.string.toHMAC

private val logger = KotlinLogging.logger {}

val refsPattern = "refs/\\w*/".toRegex()

object GitHubService {
    fun processEvent(raw: String, type: String): GithubEventData? {
        return when (type) {
            "ping" -> {
                json.decodeFromString<PingEventData>(raw)
            }
            "issues" -> {
                json.decodeFromString<IssueEventData>(raw)
            }
            "push" -> {
                json.decodeFromString<PushEventData>(raw)
            }
            "issue_comment" -> {
                json.decodeFromString<IssueCommentEventData>(raw)
            }
            "release" -> {
                json.decodeFromString<ReleaseEventData>(raw)
            }
            "pull_request" -> {
                json.decodeFromString<PullRequestEventData>(raw)
            }
            else -> {
                logger.debug("解析 WebHook 消息失败, 不支持的事件类型 ($type)")
                null
            }
        }
    }

    fun checkSecret(secret: String?, requestBody: String, eventType: String): SecretStatus {
        val parse: GithubEventData =
            processEvent(
                URLDecoder.decode(requestBody.replace("payload=", ""), Charsets.UTF_8),
                eventType
            ) ?: return SecretStatus.FAILED

        val targetRepo =
            GithubRepoData.data.repos.find { it.getName() == parse.repoName() } ?: return SecretStatus.NOT_FOUND

        if (targetRepo.secret.isEmpty() && secret == null) {
            return SecretStatus.NO_SECRET
        }

        val checkStatus = checkSignature(targetRepo.secret, secret ?: "", requestBody)

        return if (checkStatus) {
            SecretStatus.HAS_SECRET
        } else {
            SecretStatus.UNAUTHORIZED
        }
    }

    fun checkSignature(secret: String, remote: String, requestBody: String): Boolean {
        val local = "sha256=${requestBody.toHMAC(secret)}"
        logger.debug("本地解析签名为: $local, 远程签名为: $remote")
        return local == remote
    }
}

fun Comet.subscribeGithubEvent() = run {
    registerListener<GithubEvent> { event ->
        event.broadcastTargets.forEach {
            getGroup(it.id)?.sendMessage(event.eventData.toMessageWrapper())
        }
    }
}
