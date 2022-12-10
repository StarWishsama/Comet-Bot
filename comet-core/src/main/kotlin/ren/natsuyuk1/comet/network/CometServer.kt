package ren.natsuyuk1.comet.network

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.ratelimit.*
import ren.natsuyuk1.comet.network.server.githubWebHookModule
import ren.natsuyuk1.comet.network.server.pushTemplateModule
import ren.natsuyuk1.comet.network.server.remoteConsoleModule
import ren.natsuyuk1.comet.objects.config.CometServerConfig
import kotlin.time.Duration.Companion.seconds

private val logger = mu.KotlinLogging.logger("CometServer")

object CometServer {
    private lateinit var server: ApplicationEngine

    fun init() {
        if (!CometServerConfig.data.switch) {
            return
        }

        server = embeddedServer(
            Netty,
            environment = applicationEngineEnvironment {
                module {
                    install(CallLogging) {
                        logger = ren.natsuyuk1.comet.network.logger
                    }

                    install(RateLimit) {
                        register(RateLimitName("comet_pusher_limiter")) {
                            rateLimiter(limit = 60, refillPeriod = 60.seconds)

                            requestKey { call ->
                                call.request.headers["Authorization"]!!
                            }
                        }
                    }

                    githubWebHookModule()
                    pushTemplateModule()
                    remoteConsoleModule()
                }

                connector {
                    port = CometServerConfig.data.port
                }
            }
        )

        server.start(false)
        logger.info { "Comet 服务成功启动! 运行于端口 ${CometServerConfig.data.port}" }
    }

    fun stop() {
        if (::server.isInitialized) {
            server.stop(1000, 1000)
        }
    }
}
