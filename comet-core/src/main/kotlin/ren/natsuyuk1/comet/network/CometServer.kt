package ren.natsuyuk1.comet.network

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ren.natsuyuk1.comet.network.server.githubWebHookModule
import ren.natsuyuk1.comet.objects.config.CometServerConfig

private val logger = mu.KotlinLogging.logger("CometServer")

object CometServer {
    private lateinit var server: ApplicationEngine

    fun init(config: CometServerConfig) {
        server = embeddedServer(Netty, environment = applicationEngineEnvironment {
            module {
                install(CallLogging)
                githubWebHookModule()
            }

            connector {
                port = config.data.port
            }
        })

        server.start(false)
        logger.info { "Comet 服务器成功启动! 运行在端口 ${config.data.port}" }
    }

    fun stop() {
        if (::server.isInitialized) {
            server.stop(1000, 1000)
        }
    }
}
