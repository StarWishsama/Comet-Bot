package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.localizationManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.BotUser.Companion.getUser
import io.github.starwishsama.comet.utils.RuntimeUtil.getJVMVersion
import io.github.starwishsama.comet.utils.RuntimeUtil.getMaxMemory
import io.github.starwishsama.comet.utils.RuntimeUtil.getOsInfo
import io.github.starwishsama.comet.utils.RuntimeUtil.getUsedMemory
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.toFriendly
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import org.apache.commons.lang3.StringUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import javax.imageio.ImageIO
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

/**
 * 用于辅助机器人运行的各种工具方法
 *
 * @author StarWishsama
 */

fun BufferedImage.toInputStream(formatName: String = "png"): InputStream {
    ByteArrayOutputStream().use { byteOS ->
        if (ImageIO.getUseCache()) {
            ImageIO.setUseCache(false)
        }
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
    fun sendMessageAsString(otherText: String?, addPrefix: Boolean = true): String {
        if (otherText.isNullOrEmpty()) return ""
        return buildString {
            if (addPrefix) {
                append(localizationManager.getLocalizationText("prefix")).append(" ")
            }
            append(otherText)
        }.trim()
    }

    fun toChain(otherText: String?, addPrefix: Boolean = true): MessageChain =
        sendMessageAsString(otherText, addPrefix).convertToChain()

    @JvmName("stringAsChain")
    fun String?.toChain(addPrefix: Boolean = true): MessageChain = toChain(this, addPrefix)

    fun List<String>.getRestString(startAt: Int, joinRune: String = " "): String {
        if (isEmpty()) {
            return "空"
        }

        return buildString {
            for (i in startAt until size) {
                append(this@getRestString[i]).append(joinRune)
            }
        }.trim().removeSuffix(joinRune)
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

    @OptIn(ExperimentalTime::class)
    fun getMemoryUsage(): String =
        "OS 信息: ${getOsInfo()}\n" +
                "JVM 版本: ${getJVMVersion()}\n" +
                "内存占用: ${getUsedMemory()}MB/${getMaxMemory()}MB\n" +
                "运行时长: ${getRunningTime()}"
}
