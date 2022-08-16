package ren.natsuyuk1.comet.network.thirdparty.arcaea

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaCommand
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaUserInfo
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.Command
import ren.natsuyuk1.comet.utils.brotli4j.BrotliDecompressor
import ren.natsuyuk1.comet.utils.brotli4j.BrotliLoader

private val logger = KotlinLogging.logger {}

object ArcaeaClient {
    init {
        runBlocking {
            BrotliLoader.loadBrotli()
        }
    }

    private const val arcaeaAPIHost = "arc.estertion.win"
    private const val arcaeaAPIPort = 616

    suspend fun queryUserInfo(userID: String): ArcaeaUserInfo? {
        if (!BrotliDecompressor.isUsable()) {
            return null
        }

        val client = HttpClient {
            install(WebSockets)
        }

        var resp: ArcaeaUserInfo? = null

        client.wss(host = arcaeaAPIHost, port = arcaeaAPIPort) {
            send(userID)

            try {
                while (true) {
                    when (val msg = incoming.receive()) {
                        is Frame.Text -> {
                            val text = msg.readText()

                            if (text == "bye") {
                                client.close()
                            } else {
                                logger.debug { "Arcaea client received: $text" }
                            }
                        }

                        is Frame.Binary -> {
                            val incomingJson = String(BrotliDecompressor.decompress(msg.readBytes()))
                            val command: Command = json.decodeFromString(incomingJson)

                            logger.debug { "Received command: $command" }
                            logger.debug { "Received json: $incomingJson" }

                            when (command.command) {
                                ArcaeaCommand.USER_INFO -> {
                                    resp = json.decodeFromString(incomingJson)
                                    send("bye")
                                    client.close()
                                }

                                else -> { /* ignore */
                                }
                            }
                        }

                        is Frame.Close -> client.close()
                        else -> { /* ignore */
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                logger.debug { "Arcaea client closed by accident" }
            }
        }

        return resp
    }
}
