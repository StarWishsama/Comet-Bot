package io.github.starwishsama.comet.service.webhook

import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import io.github.starwishsama.comet.BotVariables.netLogger
import io.github.starwishsama.comet.logger.HinaLogLevel
import java.net.InetSocketAddress

class WebHookServer(val port: Int) {
    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0)

    init {
        server.createContext("/payload", GithubWebHookHandler())
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop(0)
    }
}

class GithubWebHookHandler: HttpHandler {
    override fun handle(he: HttpExchange) {
        netLogger.log(HinaLogLevel.Debug, "收到新事件消息", prefix = "WebHook")
        val payload = String(he.requestBody.readBytes()).replace("payload=", "")

        val validate = JsonParser.parseString(payload).isJsonObject

        if (!validate) return


    }
}

