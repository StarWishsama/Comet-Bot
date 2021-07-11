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
import cn.hutool.http.HttpStatus
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.github.GithubEventHandler
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.objects.config.SecretStatus
import io.github.starwishsama.comet.service.command.GitHubService
import io.github.starwishsama.comet.service.pusher.instances.GithubPusher
import io.github.starwishsama.comet.utils.json.isUsable
import io.github.starwishsama.comet.utils.network.writeTextResponse
import java.io.IOException

// For bypass detekt
@Suppress("unused")
object CometServerModules

/**
 * [GithubWebHookHandler]
 *
 * 处理 Github Webhook 请求.
 */
class GithubWebHookHandler : HttpHandler {
    private val signature256 = "X-Hub-Signature-256"
    private val eventTypeHeader = "X-GitHub-Event"

    override fun handle(he: HttpExchange) {
        CometVariables.netLogger.debug("有新连接 ${he.requestMethod} - ${he.requestURI}")
        CometVariables.netLogger.debug("请求 Headers ${he.requestHeaders.entries}")

        he.responseHeaders.add("content-type", "text/plain; charset=UTF-8")

        // Get information from header to identity whether the request is from GitHub.
        if (!checkOrigin(he)) {
            return
        }

        val signature = he.requestHeaders[signature256]
        val eventType = he.requestHeaders[eventTypeHeader]?.get(0) ?: ""
        val request = String(he.requestBody.readBytes())
        val secretStatus = GitHubService.repos.checkSecret(signature, request, eventType)

        if (!checkSecretStatus(he, secretStatus, signature)) {
            return
        }

        CometVariables.netLogger.log(HinaLogLevel.Debug, "收到新事件", prefix = "WebHook")

        val payload = URLDecoder.decode(request.replace("payload=", ""), Charsets.UTF_8)
        val validate = CometVariables.mapper.readTree(payload).isUsable()

        if (!validate) {
            CometVariables.netLogger.log(HinaLogLevel.Warn, "解析请求失败, 回调的 JSON 不合法.\n${payload}", prefix = "WebHook")
            he.writeTextResponse("Unknown request", statusCode = HttpStatus.HTTP_INTERNAL_ERROR)
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

                he.writeTextResponse(
                    "Comet 已收到事件, 但所请求的事件类型不支持 (${eventType})",
                    statusCode = HttpStatus.HTTP_INTERNAL_ERROR
                )

                return
            }
        } catch (e: IOException) {
            CometVariables.netLogger.log(HinaLogLevel.Warn, "推送 WebHook 消息失败", e, prefix = "WebHook")
            hasError = true
        } finally {
            when {
                secretStatus == SecretStatus.NO_SECRET -> {
                    he.writeTextResponse("Comet 已收到事件, 推荐使用密钥加密以保证服务器安全")
                }
                hasError -> {
                    he.writeTextResponse("Comet 发生内部错误", statusCode = HttpStatus.HTTP_INTERNAL_ERROR)
                }
                else -> {
                    he.writeTextResponse("Comet 已收到事件")
                }
            }

        }
    }

    private fun checkSecretStatus(
        he: HttpExchange,
        secretStatus: SecretStatus,
        signature: MutableList<String>?
    ): Boolean {
        if (signature == null && secretStatus == SecretStatus.NO_SECRET) {
            CometVariables.netLogger.log(HinaLogLevel.Debug, "收到新事件, 未通过安全验证. 请求的签名为: 无", prefix = "WebHook")
            he.writeTextResponse("A Serve error has happened", statusCode = HttpStatus.HTTP_INTERNAL_ERROR)
            return false
        }

        if (signature != null && secretStatus == SecretStatus.UNAUTHORIZED) {
            CometVariables.netLogger.log(
                HinaLogLevel.Debug,
                "收到新事件, 未通过安全验证. 请求的签名为: ${signature[0]}",
                prefix = "WebHook"
            )
            he.writeTextResponse("A Serve error has happened", statusCode = HttpStatus.HTTP_INTERNAL_ERROR)
            return false
        }

        return true
    }

    /**
     * [checkOrigin]
     *
     * 通过多种方式检测来源是否来自于 GitHub
     *
     * @return 是否为 GitHub 的请求
     */
    private fun checkOrigin(he: HttpExchange): Boolean {
        if (he.requestHeaders[eventTypeHeader] == null || he.requestHeaders["User-Agent"]?.get(0)
                ?.startsWith("GitHub-Hookshot") == false
        ) {
            CometVariables.netLogger.log(HinaLogLevel.Debug, "无效请求", prefix = "WebHook")
            he.writeTextResponse("Unsupported Request", statusCode = HttpStatus.HTTP_FORBIDDEN)
            return false
        }

        return true
    }
}
