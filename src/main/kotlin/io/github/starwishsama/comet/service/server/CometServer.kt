package io.github.starwishsama.comet.service.server

import com.sun.net.httpserver.HttpServer
import io.github.starwishsama.comet.BotVariables.netLogger
import io.github.starwishsama.comet.logger.HinaLogLevel
import io.github.starwishsama.comet.service.server.module.GithubWebHookHandler
import java.net.InetSocketAddress

class WebHookServer(port: Int, customSuffix: String) {
    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0)

    init {
        server.createContext("/$customSuffix", GithubWebHookHandler())
        netLogger.log(HinaLogLevel.Info, "已注册 Github WebHook 后缀: $customSuffix", prefix = "WebHook")
        server.createContext("/test") { he ->
            if (ServerUtil.checkCoolDown(he.remoteAddress)) {
                he.sendResponseHeaders(500, 0)
                return@createContext
            }

            he.sendResponseHeaders(200, 0)
            he.responseBody.use {
                it.write("Hello Comet!".toByteArray())
                it.flush()
            }
        }
        netLogger.log(HinaLogLevel.Info, "服务器启动! 运行在端口 $port", prefix = "WebHook")
        server.start()
    }

    fun stop() {
        server.stop(0)
    }
}

