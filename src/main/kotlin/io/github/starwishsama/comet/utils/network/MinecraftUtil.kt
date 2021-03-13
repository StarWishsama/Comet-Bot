package io.github.starwishsama.comet.utils.network

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import java.io.*
import java.net.Proxy
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * 查询 Minecraft 服务器信息
 *
 * @author LovesAsuna
 * Source from:
 * https://github.com/LovesAsuna/Mirai-Bot/blob/master/src/main/kotlin/me/lovesasuna/bot/util/protocol/QueryUtil.kt
 */
object MinecraftUtil {
    @Throws(IOException::class)
    fun query(host: String, port: Int): QueryInfo {
        val socket: Socket
        if (cfg.proxySwitch) {
            socket = Socket(Proxy(cfg.proxyType, Socket(cfg.proxyUrl, cfg.proxyPort).remoteSocketAddress))
            socket.connect(Socket(host, port).remoteSocketAddress)
        } else {
            socket = Socket(host, port)
        }

        socket.soTimeout = 10 * 1000

        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val inputStream = socket.getInputStream()
        val inputStreamReader = InputStreamReader(inputStream)
        val b = ByteArrayOutputStream()
        val handshake = DataOutputStream(b)
        /*握手数据包id*/
        handshake.writeByte(0x00)
        /*协议版本*/
        writeVarInt(handshake, 578)
        /*主机地址长度*/
        writeVarInt(handshake, host.length)
        /*主机地址*/
        handshake.writeBytes(host)
        /*端口*/
        handshake.writeShort(25565)
        /*状态(握手是1)*/
        writeVarInt(handshake, 1)

        /*发送的握手数据包大小*/
        writeVarInt(dataOutputStream, b.size())
        /*发送握手数据包*/
        dataOutputStream.write(b.toByteArray())

        /*大小为1*/
        dataOutputStream.writeByte(0x01)
        /*ping的数据包id*/
        dataOutputStream.writeByte(0x00)
        val dataInputStream = DataInputStream(inputStream)
        /*返回的数据包大小*/
        readVarInt(dataInputStream)
        /*返回的数据包id*/
        var id = readVarInt(dataInputStream)
        if (id == -1) {
            throw IOException("数据流过早结束")
        }

        /*需要返回的状态*/
        if (id != 0x00) {
            throw IOException("无效的数据包 ID")
        }
        /*json字符串长度*/
        val length = readVarInt(dataInputStream)
        if (length == -1) {
            throw IOException("数据流过早结束")
        }
        if (length == 0) {
            throw IOException("无效的 json 字符串长度")
        }
        val jsonString = ByteArray(length)
        /* 读取json字符串 */
        dataInputStream.readFully(jsonString)
        val json = String(jsonString, Charsets.UTF_8)
        val now = System.currentTimeMillis()
        /* 数据包大小 */
        dataOutputStream.writeByte(0x09)
        /* ping 0x01 */
        dataOutputStream.writeByte(0x01)
        /*时间*/
        dataOutputStream.writeLong(now)
        readVarInt(dataInputStream)
        id = readVarInt(dataInputStream)
        if (id == -1) {
            throw IOException("数据流过早结束")
        }
        if (id != 0x01) {
            throw IOException("无效的数据包 ID")
        }
        /* 读取回应 (pingtime) */
        val pingTime = dataInputStream.readLong()
        dataOutputStream.close()
        outputStream.close()
        inputStreamReader.close()
        inputStream.close()
        socket.close()
        return QueryInfo(json, pingTime)
    }

    @Throws(IOException::class)
    private fun writeVarInt(out: DataOutputStream, paramInt: Int) {
        var int = paramInt
        while (true) {
            if (int and -0x80 == 0) {
                out.writeByte(int)
                return
            }
            out.writeByte(int and 0x7F or 0x80)
            int = int ushr 7
        }
    }

    @Throws(IOException::class)
    fun readVarInt(dis: DataInputStream): Int {
        var i = 0
        var j = 0
        while (true) {
            val k = dis.readByte().toInt()
            i = i or (k and 0x7F shl j++ * 7)
            if (j > 5) {
                throw RuntimeException("VarInt too big")
            }
            if (k and 0x80 != 128) {
                break
            }
        }
        return i
    }

    fun convert(host: String): SRVConvertResult {
        return try {
            val records = Lookup("_minecraft._tcp.$host", Type.SRV).run()
            if (records != null && records.isNotEmpty()) {
                val result = records[0] as SRVRecord
                SRVConvertResult(result.target.toString().replaceFirst(Regex("\\.$"), ""), result.port)
            } else {
                SRVConvertResult("", -1)
            }
        } catch (e: Exception) {
            SRVConvertResult("", -1)
        }
    }
}

data class SRVConvertResult(
    val host: String,
    val port: Int,
) {
    fun isEmpty(): Boolean {
        return host.isEmpty() || port < 0
    }
}

data class QueryInfo(
    val json: String,
    val usedTime: Long
) {
    private fun parseJson(): MinecraftServerInfo {
        return mapper.readValue(json)
    }

    override fun toString(): String {
        val info = parseJson()

        return """
            > 在线玩家 ${info.players.onlinePlayer}/${info.players.maxPlayer}
            > MOTD ${if (info.motd.isArray) 
                info.motd["extra"]["text"].asText().trim().limitStringSize(20)
                else info.motd.asText().trim().limitStringSize(20)
            }
            > 服务器版本 ${info.version.protocolName}
            > 延迟 ${usedTime.toLocalDateTime().getLastingTimeAsString(TimeUnit.SECONDS, true)}
        """.trimIndent()
    }
}

data class MinecraftServerInfo(
    val version: Version,
    val players: PlayerInfo,
    @JsonProperty("description")
    val motd: JsonNode,
    @JsonProperty("modinfo")
    val modInfo: ModInfo?
) {
    data class Version (
        @JsonProperty("name")
        val protocolName: String,
        @JsonProperty("protocol")
        val protocolVersion: Int
    )

    data class PlayerInfo(
        @JsonProperty("max")
        val maxPlayer: Int,
        @JsonProperty("online")
        val onlinePlayer: Int
    )

    data class ModInfo(
        val type: String,
        @JsonProperty("modList")
        val modList: List<Mod>
    ) {
        data class Mod(
            @JsonProperty("modid")
            val modID: String,
            @JsonProperty("version")
            val version: String
        )
    }
}