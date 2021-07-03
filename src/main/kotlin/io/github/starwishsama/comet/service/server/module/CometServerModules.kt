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

        try {
            val info = GithubEventHandler.process(payload, eventType) ?: return CometVariables.netLogger.log(
                HinaLogLevel.Debug,
                "推送 WebHook 消息失败, 不支持的事件类型",
                prefix = "WebHook"
            )
            GithubPusher.push(info)
        } catch (e: IOException) {
            CometVariables.netLogger.log(HinaLogLevel.Warn, "推送 WebHook 消息失败", e, prefix = "WebHook")
        }

        val response = if (secretStatus == SecretStatus.NO_SECRET) {
            "Comet 已收到事件, 推荐使用密钥加密以保证服务器安全"
        } else {
            "Comet 已收到事件"
        }

        he.writeTextResponse(response)
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
            val resp = "Unsupported Request".toByteArray()
            he.sendResponseHeaders(HttpStatus.HTTP_FORBIDDEN, resp.size.toLong())
            he.responseBody.use {
                it.write(resp)
                it.flush()
            }
            return false
        }

        return true
    }
}
