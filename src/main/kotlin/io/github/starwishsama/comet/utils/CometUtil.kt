/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.CometUser.Companion.getUser
import io.github.starwishsama.comet.utils.RuntimeUtil.getJVMVersion
import io.github.starwishsama.comet.utils.RuntimeUtil.getMaxMemory
import io.github.starwishsama.comet.utils.RuntimeUtil.getOsInfo
import io.github.starwishsama.comet.utils.RuntimeUtil.getUsedMemory
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import io.github.starwishsama.comet.utils.StringUtil.toFriendly
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import javax.imageio.ImageIO
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
                append(LocalizationManager.getLocalizationText("prefix")).append(" ")
            }
            append(otherText)
        }.trim()
    }

    fun toMessageChain(otherText: String?, addPrefix: Boolean = true): MessageChain =
        sendMessageAsString(otherText, addPrefix).convertToChain()

    @JvmName("stringAsChain")
    fun String?.toMessageChain(addPrefix: Boolean = true): MessageChain = toMessageChain(this, addPrefix)

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

    fun getRunningTime(): String {
        val remain = Duration.between(CometVariables.startTime, LocalDateTime.now())
        return remain.toKotlinDuration().toFriendly()
    }

    fun parseAtToId(chain: MessageChain, possibleID: String): Long {
        chain.forEach {
            if (it is At) {
                return it.target
            }
        }

        return if (possibleID.isNumeric()) {
            possibleID.toLong()
        } else {
            -1
        }
    }

    fun parseAtAsBotUser(chain: MessageChain, id: String): CometUser? = getUser(parseAtToId(chain, id))

    fun getMemoryUsage(): String =
        "OS 信息: ${getOsInfo()}\n" +
                "JVM 版本: ${getJVMVersion()}\n" +
                "内存占用: ${getUsedMemory()}MB/${getMaxMemory()}MB\n" +
                "运行时长: ${getRunningTime()}"
}
