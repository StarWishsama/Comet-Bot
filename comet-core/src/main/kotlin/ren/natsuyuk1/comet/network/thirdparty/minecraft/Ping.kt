package ren.natsuyuk1.comet.network.thirdparty.minecraft

import ren.natsuyuk1.comet.utils.time.Timer
import java.io.*
import java.net.Socket
import java.util.concurrent.TimeUnit

enum class MinecraftServerType {
    JAVA, BEDROCK
}

suspend fun query(host: String, port: Int, serverType: MinecraftServerType): QueryInfo {
    return when (serverType) {
        MinecraftServerType.JAVA -> javaQuery(host, port)
        MinecraftServerType.BEDROCK -> bedrockQuery(host, port)
    }
}

private fun javaQuery(host: String, port: Int): QueryInfo {
    try {
        val timer = Timer()
        val socket = Socket(host, port)

        socket.soTimeout = TimeUnit.SECONDS.toMillis(5).toInt()

        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val inputStream = socket.getInputStream()
        val inputStreamReader = InputStreamReader(inputStream)
        val b = ByteArrayOutputStream()
        val handshake = DataOutputStream(b)
        /*握手数据包id*/
        handshake.writeByte(0x00)
        /*协议版本*/
        handshake.writeVarInt(578)
        /*主机地址长度*/
        handshake.writeVarInt(host.length)
        /*主机地址*/
        handshake.writeBytes(host)
        /*端口*/
        handshake.writeShort(25565)
        /*状态(握手是1)*/
        handshake.writeVarInt(1)

        /*发送的握手数据包大小*/
        dataOutputStream.writeVarInt(b.size())
        /*发送握手数据包*/
        dataOutputStream.write(b.toByteArray())

        /*大小为1*/
        dataOutputStream.writeByte(0x01)
        /*ping的数据包id*/
        dataOutputStream.writeByte(0x00)
        val dataInputStream = DataInputStream(inputStream)
        /*返回的数据包大小*/
        dataInputStream.readVarInt()
        /*返回的数据包id*/
        var id = dataInputStream.readVarInt()
        id.checkVarInt()

        /*json字符串长度*/
        val length = dataInputStream.readVarInt()
        length.checkVarInt()

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
        dataInputStream.readVarInt()
        id = dataInputStream.readVarInt()
        id.checkVarInt()

        dataOutputStream.close()
        outputStream.close()
        inputStreamReader.close()
        inputStream.close()
        socket.close()

        return QueryInfo(json, MinecraftServerType.JAVA, timer.measureDuration().inWholeMilliseconds)
    } catch (e: Exception) {
        throw e
    }
}

private fun bedrockQuery(host: String, port: Int): QueryInfo {
    TODO()
}

private fun Int.checkVarInt() {
    if (this == -1) {
        throw IllegalStateException("数据流过早关闭")
    }

    if (this != 0x01) {
        throw IllegalArgumentException("无效的数据包 ID")
    }
}

@Throws(IOException::class)
private fun DataOutputStream.writeVarInt(paramInt: Int) {
    var int = paramInt
    while (true) {
        if (int and -0x80 == 0) {
            writeByte(int)
            return
        }
        writeByte(int and 0x7F or 0x80)
        int = int ushr 7
    }
}

@Throws(IOException::class)
private fun DataInputStream.readVarInt(): Int {
    var i = 0
    var j = 0
    while (true) {
        val k = readByte().toInt()
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
