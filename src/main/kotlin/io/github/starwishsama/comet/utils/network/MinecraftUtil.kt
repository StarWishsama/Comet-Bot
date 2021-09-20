/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils.network

import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.objects.minecraft.QueryInfo
import io.github.starwishsama.comet.objects.minecraft.QueryType
import io.github.starwishsama.comet.objects.minecraft.SRVConvertResult
import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import java.io.*
import java.net.*


/**
 * 查询 Minecraft 服务器信息
 *
 * @author LovesAsuna
 * Source from:
 * https://github.com/LovesAsuna/Mirai-Bot/blob/master/src/main/kotlin/me/lovesasuna/bot/util/protocol/QueryUtil.kt
 */
object MinecraftUtil {
    @Throws(IOException::class)
    fun javaQuery(host: String, port: Int): QueryInfo {
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

        dataOutputStream.close()
        outputStream.close()
        inputStreamReader.close()
        inputStream.close()
        socket.close()

        return QueryInfo(json, QueryType.JAVA, System.currentTimeMillis() - now)
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

    /**
     * 基岩版部分
     * 代码来自: https://github.com/zixuan2020/mirai-bedrock-motd
     */

    @Throws(IOException::class)
    fun bedrockQuery(address: String, port: Int = 19132): QueryInfo {
        val socket = DatagramSocket()

        socket.use {
            socket.soTimeout = 1500
            val start = System.currentTimeMillis()
            socket.connect(InetAddress.getByName(address), port)
            val receivePacket = DatagramPacket(ByteArray(1024), 1024)

            // 数据包
            val bytes: ByteArray = convertToBedrockByte("0100000000240D12D300FFFF00FEFEFEFEFDFDFDFD12345678")
                ?: return QueryInfo("", QueryType.BEDROCK, System.currentTimeMillis() - start)
            socket.send(DatagramPacket(bytes, 0, bytes.size))

            socket.receive(receivePacket)

            val result = String(receivePacket.data, Charsets.UTF_8)

            return QueryInfo(result, QueryType.BEDROCK, System.currentTimeMillis() - start)
        }
    }

    private fun convertToBedrockByte(hexString: String): ByteArray? {
        if (hexString.isEmpty()) {
            return null
        }

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

    fun convert(host: String): SRVConvertResult {
        return try {
            val records = Lookup("_minecraft._tcp.$host", Type.SRV).run()
            if (records != null && records.isNotEmpty()) {
                val result = records[0] as SRVRecord
                SRVConvertResult(result.target.toString().replaceFirst(Regex("\\.$"), ""), result.port)
            } else {
                // 可能这个链接就是普通的 A 解析, fallback 一下
                SRVConvertResult(host, 25565)
            }
        } catch (e: Exception) {
            SRVConvertResult("", -1)
        }
    }
}