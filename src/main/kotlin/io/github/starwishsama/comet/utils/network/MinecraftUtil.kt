package io.github.starwishsama.comet.utils.network

import java.io.*
import java.net.Socket

/**
 * 查询 Minecraft 服务器信息
 *
 * @author LovesAsuna
 * Source from: https://github.com/LovesAsuna/Mirai-Bot/blob/master/src/main/java/me/lovesasuna/bot/util/protocol/QueryUtil.kt
 */
object MinecraftUtil {
    fun query(host: String, port: Int): String {
        val socket = Socket(host, port)
        socket.soTimeout = 10_000
        val socketOutStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(socketOutStream)
        val socketInStream = socket.getInputStream()
        val inputStreamReader = InputStreamReader(socketInStream)
        val b = ByteArrayOutputStream()
        val handshakePacket = DataOutputStream(b)
        /* 握手数据包 ID */
        handshakePacket.writeByte(0x00)
        /* 协议版本 */
        writeVarInt(handshakePacket, 578)
        /* 主机地址长度 */
        writeVarInt(handshakePacket, host.length)
        /* 主机地址 */
        handshakePacket.writeBytes(host)
        /* 端口 */
        handshakePacket.writeShort(25565)
        /* 状态 (握手是 1) */
        writeVarInt(handshakePacket, 1)

        /* 发送的握手数据包大小 */
        writeVarInt(dataOutputStream, b.size())
        /* 发送握手数据包 */
        dataOutputStream.write(b.toByteArray())

        /* 大小为1 */
        dataOutputStream.writeByte(0x01)
        /* ping 的数据包 ID */
        dataOutputStream.writeByte(0x00)
        val dataInputStream = DataInputStream(socketInStream)
        /* 返回的数据包大小 */
        val size = readVarInt(dataInputStream)
        /* 返回的数据包id */
        var id = readVarInt(dataInputStream)
        if (id == -1) {
            throw IOException("数据流过早结束")
        }

        /* 需要返回的状态 */
        if (id != 0x00) {
            throw IOException("无效的数据包 ID")
        }
        /* json 字符串长度 */
        val length = readVarInt(dataInputStream)
        when (length) {
            -1 -> throw IOException("数据流过早结束")
            0 -> throw IOException("无效的 json 字符串长度")
        }

        val `in` = ByteArray(length)
        /* 读取 json 字符串 */
        dataInputStream.readFully(`in`)
        val json = String(`in`, Charsets.UTF_8)
        val now = System.currentTimeMillis()
        /* 数据包大小 */
        dataOutputStream.writeByte(0x09)
        /* ping 0x01 */
        dataOutputStream.writeByte(0x01)
        /* 时间 */
        dataOutputStream.writeLong(now)
        readVarInt(dataInputStream)
        id = readVarInt(dataInputStream)
        when (id) {
            -1 -> throw IOException("数据流过早结束")
            0x01 -> throw IOException("无效的数据包 ID")
        }
        /* 读取回应 */
        val pingValue = dataInputStream.readLong()
        dataOutputStream.close()
        socketOutStream.close()
        inputStreamReader.close()
        socketInStream.close()
        socket.close()
        return json
    }

    @Throws(IOException::class)
    private fun writeVarInt(out: DataOutputStream, paramInt: Int) {
        var paramInt = paramInt
        while (true) {
            if (paramInt and -0x80 == 0) {
                out.writeByte(paramInt)
                return
            }
            out.writeByte(paramInt and 0x7F or 0x80)
            paramInt = paramInt ushr 7
        }
    }

    @Throws(IOException::class)
    fun readVarInt(`in`: DataInputStream): Int {
        var i = 0
        var j = 0
        while (true) {
            val k = `in`.readByte().toInt()
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
}