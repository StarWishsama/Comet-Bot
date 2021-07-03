/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.server

import com.sun.net.httpserver.HttpServer
import io.github.starwishsama.comet.CometVariables.netLogger
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
        netLogger.log(HinaLogLevel.Info, "服务器已关闭", prefix = "WebHook")
    }
}

