/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils

import cn.hutool.core.codec.Base64Decoder
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

enum class IDGuidelineType(val rule: Regex) {
    MINECRAFT(Regex("[a-zA-Z0-9_-]*")), UBISOFT(Regex("[a-zA-Z0-9_.-]*"))
}

object StringUtil {
    private val colorRegex = Regex("\u001B\\[[;\\d]*m")
    private val numberRegex = Regex("[-+]?\\d*\\.?\\d+")
    private val alphabetNumberRegex = Regex("[a-zA-Z0-9]*")

    fun String.withoutColor() = this.replace(colorRegex, "")

    fun Char.isNewline() = when (this) {
        '\n' -> true
        '\r' -> true
        else -> false
    }

    fun ByteArray.base64ToImage(): BufferedImage {
        if (this.isEmpty()) {
            throw IllegalArgumentException("Image byte array cannot be empty!")
        }

        val imageByte = Base64Decoder.decode(this)

        val bis = ByteArrayInputStream(imageByte)
        bis.use {
            return ImageIO.read(bis)
        }
    }

    /**
     * 来自 Mirai 的 asHumanReadable
     */
    fun Duration.toFriendly(maxUnit: TimeUnit = TimeUnit.DAYS, msMode: Boolean = true): String {
        toComponents { days, hours, minutes, seconds, ns ->
            return buildString {
                if (days != 0L && maxUnit >= TimeUnit.DAYS)
                    append("${days}天")
                if (hours != 0 && maxUnit >= TimeUnit.HOURS)
                    append("${hours}时")
                if (minutes != 0 && maxUnit >= TimeUnit.MINUTES)
                    append("${minutes}分")
                if (seconds != 0 && maxUnit >= TimeUnit.SECONDS)
                    append("${seconds}秒")
                if (maxUnit >= TimeUnit.MILLISECONDS && msMode)
                    append("${ns / 1_000_000}毫秒")
            }
        }
    }

    /**
     * 将字符串转换为消息链 [MessageChain]
     */
    fun String.convertToChain(): MessageChain {
        return PlainText(this).toMessageChain()
    }

    /**
     * 判断字符串是否为整数
     * @return 是否为整数
     */
    fun String.isNumeric(): Boolean {
        return matches(numberRegex) && !this.contains(".")
    }

    fun String.limitStringSize(size: Int): String {
        return if (this.length <= size) this else substring(0, size) + "..."
    }

    /**
     * 获取该 [LocalDateTime] 距今的时间
     *
     */
    fun LocalDateTime.getLastingTime(): Duration {
        val now = LocalDateTime.now()
        return java.time.Duration.between(min(this, now), max(this, now)).toKotlinDuration()
    }

    fun max(dateTime: LocalDateTime, other: LocalDateTime) = if (dateTime > other) dateTime else other

    fun min(dateTime: LocalDateTime, other: LocalDateTime) = if (dateTime < other) dateTime else other

    /**
     * 获取该 [LocalDateTime] 距今的时间并转换为友好的字符串
     *
     * @param msMode 是否精准到毫秒
     */
    fun LocalDateTime.getLastingTimeAsString(unit: TimeUnit = TimeUnit.SECONDS, msMode: Boolean = false): String {
        val duration = getLastingTime()
        return duration.toFriendly(maxUnit = unit, msMode = msMode)
    }

    fun String.containsEtc(strict: Boolean = true, vararg string: String): Boolean {
        var counter = 0

        string.forEach {
            if (this.contains(it)) {
                if (!strict) {
                    return true
                } else if (counter < string.size) {
                    counter++
                }
            }
        }

        return counter >= string.size
    }

    /**
     * 判断ID是否符合账号昵称格式规范
     *
     * @author StarWishsama
     * @param username 用户名
     * @return 是否符合规范
     */
    fun isLegitId(username: String, type: IDGuidelineType): Boolean = type.rule.matches(username)

    fun simplyClassName(name: String): String {
        return buildString {
            val classPart = name.split(".")
            classPart.forEach {
                if (it != classPart.last()) {
                    append(it.substring(0, 1))
                    append(".")
                } else {
                    append(it)
                }
            }
        }
    }

    fun isAlphabeticAndDigit(input: String): Boolean {
        return input.matches(alphabetNumberRegex)
    }

    fun String.removeTrailingNewline(includeSpace: Boolean = false): String {
        if (this.isEmpty()) return this

        var newStr = this
        while (
            newStr.lastOrNull()?.isNewline() == true ||
            (includeSpace && newStr.lastOrNull()?.isWhitespace() == true)
        ) {
            newStr = newStr.dropLast(1)
        }
        return newStr
    }

    private const val HEX_BIT = 16

    /**
     * to hex by little endian
     * @param lineSize minimal unit are 1 bytes, i.e., 8 bit
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    fun String.toHex(lineSize: Int = 8, padding: Boolean = true): String {
        require(lineSize >= 1) { "Line Size parameter must be >= 1." }
        return toByteArray().toUByteArray().asSequence()
            .windowed(1, 1)
            .map { (byte) ->
                byte.toString(HEX_BIT)
            }
            .windowed(lineSize, lineSize, partialWindows = true)
            .map {
                if (!padding) return@map it
                if (it.size >= lineSize) return@map it
                buildList {
                    addAll(it)
                    repeat(lineSize - it.size) {
                        add("00")
                    }
                }
            }
            .joinToString(separator = "\n") {
                it.joinToString(" ")
            }
    }
}
