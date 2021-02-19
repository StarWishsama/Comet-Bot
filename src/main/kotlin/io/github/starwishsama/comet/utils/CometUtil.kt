package io.github.starwishsama.comet.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.BotUser.Companion.getUser
import io.github.starwishsama.comet.utils.RuntimeUtil.getJVMVersion
import io.github.starwishsama.comet.utils.RuntimeUtil.getMaxMemory
import io.github.starwishsama.comet.utils.RuntimeUtil.getOsInfo
import io.github.starwishsama.comet.utils.RuntimeUtil.getUsedMemory
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.toFriendly
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.apache.commons.lang3.StringUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.time.Duration
import java.time.LocalDateTime
import javax.imageio.ImageIO
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration


/**
 * 用于辅助机器人运行的各种工具方法
 *
 * @author Nameless
 */

fun BufferedImage.toInputStream(formatName: String = "png"): InputStream {
    ByteArrayOutputStream().use { byteOS ->
        if (!ImageIO.getUseCache()) ImageIO.setUseCache(false)
        ImageIO.createImageOutputStream(byteOS).use { imOS ->
            ImageIO.write(this, formatName, imOS)
            return ByteArrayInputStream(byteOS.toByteArray())
        }
    }
}

fun BufferedImage.uploadAsImage(contact: Contact): Image {
    return runBlocking { toInputStream().use { it.uploadAsImage(contact) } }
}

object CometUtil {
    /**
     * 获取本地化文本
     *
     * @author NamelessSAMA
     * @param node 本地化文本节点
     * @return 本地化文本
     */
    fun getLocalMessage(node: String): String {
        for ((n, t) in BotVariables.localMessage) {
            if (n.contentEquals(node)) {
                return t
            }
        }
        return "PlaceHolder"
    }

    fun sendMessageAsString(otherText: String?, addPrefix: Boolean = true): String {
        if (otherText.isNullOrEmpty()) return ""
        return buildString {
            if (addPrefix) {
                append(getLocalMessage("msg.bot-prefix")).append(" ")
            }
            append(otherText)
        }.trim()
    }

    fun sendMessage(otherText: String?, addPrefix: Boolean = true): MessageChain = sendMessageAsString(otherText, addPrefix).convertToChain()

    @JvmName("stringAsChain")
    fun String?.sendMessage(addPrefix: Boolean = true): MessageChain = sendMessage(this, addPrefix)

    fun List<String>.getRestString(startAt: Int): String {
        if (isEmpty()) {
            return "空"
        }

        return buildString {
            for (i in startAt until size) {
                append(this@getRestString[i]).append(" ")
            }
            trim()
        }
    }

    @ExperimentalTime
    fun getRunningTime(): String {
        val remain = Duration.between(BotVariables.startTime, LocalDateTime.now())
        return remain.toKotlinDuration().toFriendly()
    }

    fun parseAtToId(event: MessageEvent, possibleID: String): Long {
        event.message.forEach {
            if (it is At) {
                return it.target
            }
        }

        return if (StringUtils.isNumeric(possibleID)) {
           possibleID.toLong()
        } else {
            -1
        }
    }

    fun parseAtAsBotUser(event: MessageEvent, id: String): BotUser? = getUser(parseAtToId(event, id))

    @ExperimentalTime
    fun getMemoryUsage(): String =
        "OS 信息: ${getOsInfo()}\n" +
                "JVM 版本: ${getJVMVersion()}\n" +
                "内存占用: ${getUsedMemory()}MB/${getMaxMemory()}MB\n" +
                "运行时长: ${getRunningTime()}"

    fun isValidJson(json: String): Boolean {
        val jsonElement: JsonElement = try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            return false
        }
        return jsonElement.isJsonObject
    }

    fun isValidJson(element: JsonElement): Boolean {
        return element.isJsonObject || element.isJsonArray
    }

    fun getNickNameByQID(qid: Long): String {
        try {
            NetUtil.executeHttpRequest("https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?g_tk=1518561325&uins=$qid").body.use {
                val response = it?.string()

                if (response != null) {
                    if (!response.startsWith("portraitCallBack(")) {
                        return "无"
                    }

                    val jsonObject =
                        JsonParser.parseString(response.replace("portraitCallBack(", "").removeSuffix(")"))

                    if (!jsonObject.isJsonObject) {
                        return "无"
                    }

                    return String(jsonObject.asJsonObject[qid.toString()].asJsonArray[6].asString.toByteArray(Charset.forName("GB2312")), Charsets.UTF_8)
                } else {
                    return "无"
                }
            }
        } catch (e: Exception) {
            return "无"
        }
    }
}
