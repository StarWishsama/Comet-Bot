package io.github.starwishsama.comet.service.webhook

import cn.hutool.core.net.URLDecoder
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.BotVariables.netLogger
import io.github.starwishsama.comet.api.thirdparty.github.PushEvent
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.service.pusher.instances.GithubPusher
import java.net.InetSocketAddress

class WebHookServer(port: Int) {
    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0)

    init {
        server.createContext("/payload", GithubWebHookHandler())
        netLogger.log(HinaLogLevel.Info, "服务器启动!", prefix = "WebHook")
        server.start()
    }

    fun stop() {
        server.stop(0)
    }
}

class GithubWebHookHandler: HttpHandler {
    override fun handle(he: HttpExchange) {
        netLogger.log(HinaLogLevel.Debug, "收到新事件消息", prefix = "WebHook")

        val request = String(he.requestBody.readBytes())

        if (!request.startsWith("payload")) {
            netLogger.log(HinaLogLevel.Debug, "无效请求", prefix = "WebHook")
            return
        }

        val payload = URLDecoder.decode(request.replace("payload=", ""), Charsets.UTF_8)

        val validate = JsonParser.parseString(payload).isJsonObject

        if (!validate) {
            netLogger.log(HinaLogLevel.Debug, "解析请求失败, \n${payload}", prefix = "WebHook")
            return
        }

        try {
            val info = gson.fromJson<PushEvent>(payload)
            GithubPusher.push(info)
        } catch (e: Exception) {
            daemonLogger.warning("推送 WebHook 消息失败, ${e.message}")
        }

        he.sendResponseHeaders(202, 0)
    }
}

