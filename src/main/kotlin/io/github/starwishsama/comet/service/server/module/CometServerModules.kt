/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.server.module

import cn.hutool.core.net.URLDecoder
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.github.GithubEventHandler
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.objects.config.SecretStatus
import io.github.starwishsama.comet.service.command.GitHubService
import io.github.starwishsama.comet.service.pusher.instances.GithubPusher
import io.github.starwishsama.comet.utils.serialize.isUsable
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.IOException

fun Application.defaultModule() {
    routing {
        defaultCallback()
    }
}

fun Application.githubWebHookModule(customSuffix: String) {
    routing {
        handleWebHook(customSuffix)
    }
}

internal fun Routing.defaultCallback() {
    get("/") {
        call.respondText("The request path is ${this.call.request.queryParameters.entries()}")
    }
}

internal fun Routing.handleWebHook(customSuffix: String) {
    post(path = "/$customSuffix") {
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
            CometVariables.netLogger.debug("有新连接 ${call.request.httpMethod} - ${call.request.uri}")
            CometVariables.netLogger.debug("请求 Headers ${call.request.headers.entries()}")

            // Get information from header to identity whether the request is from GitHub.
            if (!checkOrigin(call)) {
                CometVariables.netLogger.log(HinaLogLevel.Debug, "无效请求", prefix = "WebHook")
                call.respond(HttpStatusCode.Forbidden, "Unsupported Request")
                return
            }

            val signature = call.request.header(signature256)
            val eventType = call.request.header(eventTypeHeader) ?: ""
            val request = call.receiveText()
            val secretStatus = GitHubService.repos.checkSecret(signature, request, eventType)

            CometVariables.netLogger.log(HinaLogLevel.Debug, "收到新事件", prefix = "WebHook")

            if (!checkSecretStatus(call, secretStatus, signature)) {
                CometVariables.netLogger.log(HinaLogLevel.Debug, "Secret 校验失败", prefix = "WebHook")
                return
            }

            val payload = URLDecoder.decode(request.replace("payload=", ""), Charsets.UTF_8)
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
                CometVariables.netLogger.log(HinaLogLevel.Warn, "推送 WebHook 消息失败", e, prefix = "WebHook")
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
            }
        } catch (e: Exception) {
            CometVariables.netLogger.log(HinaLogLevel.Warn, "推送 WebHook 消息失败", e, prefix = "WebHook")
            call.respondText("Comet 发生内部错误", status = HttpStatusCode.InternalServerError)
        }
    }

    private suspend fun checkSecretStatus(
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
    }

    /**
     * [checkOrigin]
     *
     * 通过多种方式检测来源是否来自于 GitHub
     *
     * @return 是否为 GitHub 的请求
     */
    private fun checkOrigin(call: ApplicationCall): Boolean {
        return call.request.httpMethod == HttpMethod.Post && call.request.header(eventTypeHeader) != null
    }
}
