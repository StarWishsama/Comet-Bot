package ren.natsuyuk1.comet.network.server

import cn.hutool.core.net.URLDecoder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.event.pusher.github.GitHubEvent
import ren.natsuyuk1.comet.network.server.response.CometResponse
import ren.natsuyuk1.comet.network.server.response.respond
import ren.natsuyuk1.comet.network.server.response.toJson
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.objects.github.data.SecretStatus
import ren.natsuyuk1.comet.service.GitHubService
import ren.natsuyuk1.comet.utils.error.ErrorHelper
import ren.natsuyuk1.comet.utils.ktor.asReadable

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
        logger.debug { "有新连接 ${call.request.httpMethod} - ${call.request.uri}" }
        logger.debug {
            "Request Headers: ${call.request.headers.asReadable()}"
        }

        try {
            // Get information from header to identity whether the request is from GitHub.
            if (!isGitHubRequest(call.request)) {
                logger.debug { "Github Webhook 传入无效请求" }
                call.respond(
                    HttpStatusCode.Forbidden,
                    CometResponse(HttpStatusCode.Forbidden, "Unsupport Request")
                        .toJson()
                )
                return
            }

            val signature = call.request.header(signature256)
            val eventType = call.request.header(eventTypeHeader) ?: ""
            val request = call.receiveText()
            val secretStatus = GitHubService.checkSecret(signature, request, eventType)

            logger.debug { "GitHub WebHook 收到新事件, secretStatus = $secretStatus" }

            if (!checkSecretStatus(call, secretStatus, signature)) {
                return
            }

            val payload = URLDecoder.decode(request.replace("payload=", ""), Charsets.UTF_8)

            logger.debug("接收到传入请求: $payload")

            var hasError = false

            try {
                val event = GitHubService.processEvent(payload, eventType)

                if (event?.isSendableEvent() == true) {
                    GitHubRepoData.find(event.repoName())?.let {
                        GitHubEvent(it, event).broadcast()
                    }
                } else {
                    return
                }
            } catch (e: Exception) {
                ErrorHelper.createErrorReportFile("推送 WebHook 消息失败", "GitHub WebHook", e, payload)
                hasError = true
            }

            when {
                hasError -> {
                    CometResponse(HttpStatusCode.InternalServerError, "Comet 发生内部错误")
                        .respond(call)
                }

                secretStatus == SecretStatus.FOUND -> {
                    CometResponse(HttpStatusCode.OK, "Comet 成功接收事件, 推荐使用密钥加密以保证安全")
                        .respond(call)
                }

                secretStatus == SecretStatus.FOUND_WITH_SECRET -> {
                    CometResponse(HttpStatusCode.OK, "Comet 成功接收事件")
                        .respond(call)
                }
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
        signature: String?
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

                call.respondText(
                    CometResponse(
                        HttpStatusCode.NotAcceptable,
                        "Comet 已收到事件, 但所请求的事件类型不支持"
                    )
                        .toJson(),
                    status = HttpStatusCode.InternalServerError
                )

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
