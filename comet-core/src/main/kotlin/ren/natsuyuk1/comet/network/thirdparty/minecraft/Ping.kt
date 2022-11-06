package ren.natsuyuk1.comet.network.thirdparty.minecraft

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.Charset

private val logger = KotlinLogging.logger {}
private const val VERSION_NUMBER = 760 // Represent Minecraft 1.19.2
private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

enum class MinecraftServerType {
    JAVA, BEDROCK
}

suspend fun query(host: String, port: Int, serverType: MinecraftServerType): QueryInfo? {
    return when (serverType) {
        MinecraftServerType.JAVA -> javaQuery(host, port)
        MinecraftServerType.BEDROCK -> bedrockQuery(host, port)
    }
}

suspend fun javaQuery(host: String, port: Int): QueryInfo? {
    val manager = SelectorManager(Dispatchers.IO)

    try {
        val socket = aSocket(manager).tcp().connect(host, port)

        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)

        val handshakePacket = BytePacketBuilder().apply {
            writeByte(0x00) // handshake packet id
            writeVarInt(VERSION_NUMBER) // version number
            writeVarInt(host.length) // host length
            writeFully(host.toByteArray()) // host
            writeUShort(port.toUShort()) // port
            writeVarInt(1) // status, 1 for handshake
        }

        sendChannel.writeVarInt(handshakePacket.size) // prepend size
        sendChannel.writePacket(handshakePacket.build())

        sendChannel.writeByte(0x01) // size == 1
        sendChannel.writeByte(0x00) // packet id for ping

        val requestTime = System.currentTimeMillis()

        receiveChannel.readVarInt() // packet size
        val id = receiveChannel.readVarInt()

        if (id == -1) {
            throw IOException("请求流过早关闭.")
        }

        if (id != 0x00) {
            throw IOException("无效的包 ID")
        }

        val respLength = receiveChannel.readVarInt()

        if (respLength == -1) {
            throw IOException("请求流过早关闭.")
        }

        if (respLength == 0) {
            throw IllegalStateException("回报的响应字符串大小有误.")
        }

        val resp = ByteArray(respLength)
        receiveChannel.readFully(resp)

        val now = System.currentTimeMillis()

        sendChannel.writeByte(0x09)
        sendChannel.writeByte(0x01)
        sendChannel.writeLong(now)

        receiveChannel.readVarInt() // drop
        receiveChannel.readVarInt() // drop

        val pingTime = receiveChannel.readLong()

        withContext(Dispatchers.IO) {
            socket.close()
            manager.close()
        }

        return QueryInfo(resp.toString(Charset.defaultCharset()), MinecraftServerType.JAVA, pingTime - requestTime)
    } catch (e: Exception) {
        logger.warn(e) { "查询 Java 版服务器失败, host=$host, port=$port" }
        return null
    }
}

suspend fun ByteWriteChannel.writeVarInt(value: Int) {
    var temp = value
    while (true) {
        if (temp and SEGMENT_BITS.inv() == 0) {
            writeByte(temp.toByte())
            return
        }
        writeByte((temp and SEGMENT_BITS or CONTINUE_BIT).toByte())

        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        temp = temp ushr 7
    }
}

fun BytePacketBuilder.writeVarInt(value: Int) {
    var temp = value
    while (true) {
        if (temp and SEGMENT_BITS.inv() == 0) {
            writeByte(temp.toByte())
            return
        }
        writeByte((temp and SEGMENT_BITS or CONTINUE_BIT).toByte())

        // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
        temp = temp ushr 7
    }
}

suspend fun ByteReadChannel.readVarInt(): Int {
    var value = 0
    var position = 0
    var currentByte: Byte
    while (true) {
        currentByte = readByte()
        value = value or (currentByte.toInt() and SEGMENT_BITS shl position)
        if (currentByte.toInt() and CONTINUE_BIT == 0) break
        position += 7
        if (position >= 32) throw RuntimeException("VarInt is too big")
    }
    return value
}

private suspend fun bedrockQuery(address: String, port: Int = 19132): QueryInfo {
    val manager = SelectorManager(Dispatchers.IO)
    val start = System.currentTimeMillis()
    val socket = aSocket(manager).udp().connect(InetSocketAddress(address, port))
    val writer = socket.openWriteChannel(true)
    val reader = socket.openReadChannel()

    val bytes: ByteArray = convertToBedrockByte("0100000000240D12D300FFFF00FEFEFEFEFDFDFDFD12345678")
    writer.writeFully(bytes)

    val byteBuffer = ByteBuffer.allocate(1024)

    reader.readFully(byteBuffer)

    val result = Charsets.UTF_8.decode(byteBuffer).toString()

    withContext(Dispatchers.IO) {
        socket.close()
        manager.close()
    }

    return QueryInfo(result, MinecraftServerType.BEDROCK, System.currentTimeMillis() - start)
}

private fun convertToBedrockByte(hexString: String): ByteArray {
    val lowerHex = hexString.lowercase()

    val byteArray = ByteArray(lowerHex.length shr 1)

    var index = 0

    for (i in lowerHex.indices) {
        if (index > lowerHex.length - 1) return byteArray
        val highDit = lowerHex[index].digitToInt(16) and 0xFF
        val lowDit = lowerHex[index + 1].digitToInt(16) and 0xFF
        byteArray[i] = (highDit shl 4 or lowDit).toByte()
        index += 2
    }

    return byteArray
}
