package ren.natsuyuk1.comet.network.thirdparty.arcaea

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaCommand
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaUserInfo
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.Command
import ren.natsuyuk1.comet.utils.brotli4j.BrotliDecompressor

private val logger = KotlinLogging.logger {}

object ArcaeaClient {
    private const val arcaeaAPIHost = "arc.estertion.win"
    private const val arcaeaAPIPort = 616

    private val songInfo = mutableMapOf<String, String>()

    suspend fun fetchConstants() {
        val cmd = "constants"
        if (!BrotliDecompressor.isUsable()) {
            return
        }

        val client = HttpClient {
            install(WebSockets)
        }

        client.wss(host = arcaeaAPIHost, port = arcaeaAPIPort) {
            send(cmd)

            while (true) {
                when (val msg = incoming.receive()) {
                    is Frame.Text -> {}
                    is Frame.Binary -> {
                        val incomingJson = String(BrotliDecompressor.decompress(msg.readBytes()))
                        val command: Command = json.decodeFromString(incomingJson)

                        logger.debug { "Received command: $command" }
                        logger.debug { "Received json: $incomingJson" }

                        when (command.command) {
                            ArcaeaCommand.SONG_TITLE -> {
                                if (songInfo.isEmpty()) {
                                    val songInfoData = json.parseToJsonElement(incomingJson)

                                    songInfoData.jsonObject["data"]?.jsonObject?.forEach { id, songName ->
                                        songInfo[id] = songName.jsonObject["en"]?.jsonPrimitive?.content!!
                                    }
                                }

                                println(songInfo.size)
                                client.close()
                                break
                            }

                            else -> { /* ignore */ }
                        }
                    }

                    else -> {
                        client.close()
                        break
                    }
                }
            }
        }
    }

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
                                    logger.debug { "Receive user info ${resp?.data?.userID} >> $resp" }
                                    send("bye")
                                    client.close()
                                }

                                else -> { /* ignore */ }
                            }
                        }

                        is Frame.Close -> client.close()
                        else -> { /* ignore */ }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                logger.debug { "Arcaea client closed by accident" }
            }
        }

        return resp
    }

    fun getSongNameByID(id: String): String = songInfo[id] ?: id
}
