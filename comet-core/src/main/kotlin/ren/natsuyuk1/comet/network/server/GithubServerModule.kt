package ren.natsuyuk1.comet.network.server

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

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
        try {
            logger.debug { "有新连接 ${call.request.httpMethod} - ${call.request.uri}" }
            logger.debug { "请求 Headers ${buildString { call.request.headers.forEach { k, v -> append("$k=$v") } }}}" }

            // Get information from header to identity whether the request is from GitHub.
            if (!isGitHubRequest(call.request)) {
                logger.debug { "Github Webhook 传入无效请求" }
                call.respond(HttpStatusCode.Forbidden, "Unsupported Request")
                return
            }

            val signature = call.request.header(signature256)
            val eventType = call.request.header(eventTypeHeader) ?: ""
            val request = call.receiveText()
            //val secretStatus = GitHubService.repos.checkSecret(signature, request, eventType)

            logger.debug { "GitHub WebHook 收到新事件" }

            /**if (!checkSecretStatus(call, secretStatus, signature)) {
            CometVariables.netLogger.log(HinaLogLevel.Debug, "Secret 校验失败", prefix = "WebHook")
            return
            }

            val payload = URLDecoder.decode(request.replace("payload=", ""), Charsets.UTF_8)

            CometVariables.netLogger.log(HinaLogLevel.Debug, "接收到传入请求: $payload")

            val validate = CometVariables.mapper.readTree(payload).isUsable()

            if (!validate) {
            CometVariables.netLogger.log(HinaLogLevel.Warn, "解析请求失败, 回调的 JSON 不合法.\n${payload}", prefix = "WebHook")
            call.respondText("Unknown request", status = HttpStatusCode.InternalServerError)
            return
            }

            var hasError = false

            try {
            val info = GithubEventHandler.process(payload, eventType)
            if (info != null) {
            GithubPusher.push(info)
            } else {
            CometVariables.netLogger.log(
            HinaLogLevel.Debug,
            "推送 WebHook 消息失败, 不支持的事件类型",
            prefix = "WebHook"
            )

            call.respondText(
            "Comet 已收到事件, 但所请求的事件类型不支持 (${eventType})",
            status = HttpStatusCode.InternalServerError
            )

            return
            }
            } catch (e: IOException) {
            FileUtil.createErrorReportFile("推送 WebHook 消息失败", "Github Webhook", e, payload, e.message ?: "")
            hasError = true
            }

            when {
            secretStatus == SecretStatus.NO_SECRET -> {
            call.respondText("Comet 已收到事件, 推荐使用密钥加密以保证服务器安全")
            }
            hasError -> {
            call.respondText("Comet 发生内部错误", status = HttpStatusCode.InternalServerError)
            }
            else -> {
            call.respondText("Comet 已收到事件")
            }
            }*/
        } catch (e: Exception) {
            logger.warn(e) { "推送 WebHook 消息失败" }
            call.respondText("Comet 发生内部错误", status = HttpStatusCode.InternalServerError)
        }
    }

    /**private suspend fun checkSecretStatus(
    call: ApplicationCall,
    secretStatus: SecretStatus,
    signature: String?
    ): Boolean {
    if (secretStatus == SecretStatus.HAS_SECRET && signature != null) {
    return true
    }

    if (signature == null && secretStatus == SecretStatus.NO_SECRET) {
    return true
    }

    if (secretStatus == SecretStatus.FAILED) {
    CometVariables.netLogger.log(
    HinaLogLevel.Debug,
    "获取 Secret 失败",
    prefix = "WebHook"
    )
    call.respondText(status = HttpStatusCode.InternalServerError, text = "Internal Server Error")
    return false
    }

    if (secretStatus == SecretStatus.UNAUTHORIZED) {
    CometVariables.netLogger.log(
    HinaLogLevel.Debug,
    "收到新事件, 未通过安全验证. 请求的签名为: ${signature?.get(0) ?: "无"}",
    prefix = "WebHook"
    )
    call.respondText(status = HttpStatusCode.Forbidden, text = "The request isn't verified")
    return false
    }

    return false
    }*/

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