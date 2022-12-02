package ren.natsuyuk1.comet.network.thirdparty.arcaea

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.isActive
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.arcaea.ArcaeaHelper.songInfo
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaCommand
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaSongInfo
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.ArcaeaUserInfo
import ren.natsuyuk1.comet.network.thirdparty.arcaea.data.Command
import ren.natsuyuk1.comet.utils.brotli4j.BrotliDecompressor
import ren.natsuyuk1.comet.utils.datetime.toFriendly
import ren.natsuyuk1.comet.utils.time.Timer
import java.util.*

private val logger = KotlinLogging.logger {}

object ArcaeaClient {
    private const val arcaeaAPIHost = "arc.estertion.win"
    private const val arcaeaAPIPort = 616

    private val queryingUser = mutableSetOf<UUID>()

    suspend fun fetchConstants(): MutableMap<String, String> {
        val cmd = "constants"
        if (!BrotliDecompressor.isUsable()) {
            return mutableMapOf()
        }

        if (songInfo.isNotEmpty()) return mutableMapOf()

        val client = HttpClient {
            install(WebSockets)
        }

        val result = mutableMapOf<String, String>()

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
                                val songInfoData = json.parseToJsonElement(incomingJson)

                                songInfoData.jsonObject["data"]?.jsonObject?.forEach { id, songName ->
                                    result[id] = songName.jsonObject["en"]?.jsonPrimitive?.content!!
                                }

                                logger.info { "已更新 Arcaea 歌曲信息 (${songInfo.size} 个)" }

                                client.close()
                                break
                            }

                            else -> { /* ignore */
                            }
                        }
                    }

                    else -> {
                        client.close()
                        break
                    }
                }
            }

            if (client.isActive) {
                client.close()
            }
        }

        return result
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
                while (client.isActive) {
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
                                    client.close()
                                    break
                                }

                                else -> { /* ignore */
                                }
                            }
                        }

                        is Frame.Close -> {
                            client.close()
                            break
                        }

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

    fun isUserQuerying(uuid: UUID) = queryingUser.contains(uuid)

    fun getQueryUserCount() = queryingUser.size

    suspend fun queryUserB38(userID: String, uuid: UUID): Pair<ArcaeaUserInfo?, List<ArcaeaSongInfo>> {
        if (!BrotliDecompressor.isUsable()) {
            return Pair(null, emptyList())
        }

        queryingUser.add(uuid)

        val songResults = mutableListOf<ArcaeaSongInfo>()
        var userInfo: ArcaeaUserInfo? = null

        val client = HttpClient {
            install(WebSockets)
        }

        val timer = Timer()

        client.wss(host = arcaeaAPIHost, port = arcaeaAPIPort) {
            send(userID)

            try {
                while (client.isActive) {
                    when (val msg = incoming.receive()) {
                        is Frame.Text -> {
                            val text = msg.readText()

                            if (text == "bye") {
                                logger.debug { "Query end, good bye!" }
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
                                    userInfo = json.decodeFromString(incomingJson)
                                    logger.debug { "Receive user info $userID >> $userID" }
                                }

                                ArcaeaCommand.SCORES -> {
                                    val playResult: ArcaeaSongInfo = json.decodeFromString(incomingJson)
                                    logger.debug { "Receive scores $userID >> $playResult" }
                                    songResults.add(playResult)
                                }

                                else -> { /* ignore */
                                }
                            }
                        }

                        is Frame.Close -> {
                            client.close()
                            break
                        }

                        else -> { /* ignore */
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                logger.debug { "Arcaea client closed by accident" }
            }
        }

        if (userInfo == null) {
            return Pair(null, emptyList())
        }

        logger.debug { "Accumulated ${songResults.size} play results, costs ${timer.measureDuration().toFriendly()}" }

        val result = songResults
            .sortedByDescending { it.songResult.first().rating }
            .take(38)
            .also { queryingUser.remove(uuid) }
        return userInfo!! to result
    }
}
