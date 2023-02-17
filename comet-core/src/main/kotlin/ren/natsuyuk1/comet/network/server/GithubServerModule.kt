package ren.natsuyuk1.comet.network.server

import cn.hutool.core.net.URLDecoder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.event.pusher.github.GitHubEvent
import ren.natsuyuk1.comet.network.server.response.CometResponse
import ren.natsuyuk1.comet.network.server.response.respond
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.objects.github.data.SecretStatus
import ren.natsuyuk1.comet.service.GitHubService
import ren.natsuyuk1.comet.utils.error.ErrorHelper

private val logger = mu.KotlinLogging.logger {}

fun Application.githubWebHookModule() {
    routing {
        handleWebHook()
    }
}

internal fun Routing.handleWebHook() {
    post(path = "/github") {
        GithubWebHookHandler.handle(call)
    }
}

/**
 * [GithubWebHookHandler]
 *
 * 处理 Github Webhook 请求.
 */
object GithubWebHookHandler {
    private const val signature256 = "X-Hub-Signature-256"
    private const val eventTypeHeader = "X-GitHub-Event"

    suspend fun handle(call: ApplicationCall) {
        // Get information from header to identity whether the request is from GitHub.
        if (!isGitHubRequest(call.request)) {
            logger.debug { "Github Webhook 传入无效请求" }
            CometResponse(HttpStatusCode.Forbidden, "Unsupport Request").respond(call)
            return
        }

        try {
            val signature = call.request.header(signature256)
            val eventType = call.request.header(eventTypeHeader)
            val request = call.receiveText()
            val secretStatus = GitHubService.checkSecret(signature, request, eventType)

            logger.debug { "GitHub WebHook 收到新事件, secretStatus = $secretStatus" }

            if (!checkSecretStatus(call, secretStatus, signature)) {
                logger.debug { "检查密钥状态失败" }
                CometResponse(
                    HttpStatusCode.Unauthorized,
                    "Check failed",
                ).respond(call)
                return
            }

            val payload = URLDecoder.decode(request.replace("payload=", ""), Charsets.UTF_8)

            logger.debug("接收到传入请求: $payload")

            try {
                val event = GitHubService.processEvent(payload, eventType)

                if (event?.isSendableEvent() == true) {
                    GitHubRepoData.find(event.repoName())?.let {
                        GitHubEvent(it, event).broadcast()
                    }
                } else {
                    CometResponse(
                        HttpStatusCode.InternalServerError,
                        "推送失败: ${if (event == null) "解析事件失败" else "对应事件不可发送"}",
                    ).respond(call)
                    return
                }
            } catch (e: Exception) {
                ErrorHelper.createErrorReportFile("推送 WebHook 消息失败", "GitHub WebHook", e, payload)
                CometResponse(HttpStatusCode.InternalServerError, "Comet 发生内部错误")
                    .respond(call)
                return
            }

            when (secretStatus) {
                SecretStatus.FOUND -> {
                    CometResponse(HttpStatusCode.OK, "Comet 成功接收事件, 推荐使用密钥加密以保证安全")
                        .respond(call)
                }

                SecretStatus.FOUND_WITH_SECRET -> {
                    CometResponse(HttpStatusCode.OK, "Comet 成功接收事件")
                        .respond(call)
                }

                else -> {}
            }
        } catch (e: Exception) {
            logger.warn(e) { "推送 WebHook 消息失败" }
            CometResponse(HttpStatusCode.InternalServerError, "Comet 发生内部错误")
                .respond(call)
        }
    }

    private suspend fun checkSecretStatus(
        call: ApplicationCall,
        secretStatus: SecretStatus,
        signature: String?,
    ): Boolean {
        return when (secretStatus) {
            SecretStatus.FOUND_WITH_SECRET -> signature != null
            SecretStatus.FOUND -> true
            SecretStatus.UNAUTHORIZED -> {
                logger.debug { "收到新事件, 未通过安全验证. 请求的签名为: ${signature?.firstOrNull() ?: "无"}" }
                CometResponse(HttpStatusCode.Forbidden, "未通过安全验证").respond(call)
                false
            }

            SecretStatus.UNSUPPORTED_EVENT -> {
                logger.debug("推送 WebHook 消息失败, 不支持的事件类型")

                CometResponse(
                    HttpStatusCode.NotAcceptable,
                    "Comet 已收到事件, 但所请求的事件类型不支持",
                ).respond(call)

                false
            }

            else -> false
        }
    }

    /**
     * [isGitHubRequest]
     *
     * 通过多种方式检测来源是否来自于 GitHub
     *
     * @return 是否为 GitHub 的请求
     */
    private fun isGitHubRequest(req: ApplicationRequest): Boolean {
        return req.httpMethod == HttpMethod.Post && req.header(eventTypeHeader) != null
    }
}
