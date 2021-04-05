package io.github.starwishsama.comet.service.server

import io.github.starwishsama.comet.logger.HinaLogLevel
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class CometServer(val port: Int) {
    lateinit var server: ApplicationEngine
    lateinit var githubPrefix: String

    init {
        try {
            server = embeddedServer(Netty, environment = applicationEngineEnvironment {
                module {
                    handleGithub(githubPrefix)
                }
                connector {
                    this.port = port
                }
            })
        } catch (e: Exception) {
            logger.warning("Comet 服务端启动失败", e)
        }
    }

    /**
     * 启动 Comet HTTP 端
     */
    fun start() {
        logger.log(HinaLogLevel.Info, "服务器启动! 运行在端口 $port")
        server.start(false)
    }

    /**
     * 关闭 Comet HTTP 端
     */
    fun stop() {
        logger.log(HinaLogLevel.Info, "正在关闭服务器")
        server.stop(5_000, 5_000)
    }
}