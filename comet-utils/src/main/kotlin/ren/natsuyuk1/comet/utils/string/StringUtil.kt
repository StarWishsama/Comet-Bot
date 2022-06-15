/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.utils.string

import cn.hutool.core.codec.Base64Decoder
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

    /**
     * 以特定格式分割字符串
     *
     * @param spiltRune 分割字符串分割符
     */
    fun String.toArgs(joinRune: String = " "): List<String> {
        if (isEmpty()) {
            return listOf()
        }

        return trim().split(joinRune.toRegex())
    }

    /**
     * 以特定格式合并字符串
     *
     * @param startAt 开始合并的位置
     * @param joinRune 合并时相邻位置的字符串
     */
    fun List<String>.joinToString(startAt: Int, joinRune: String = " "): String {
        if (isEmpty()) {
            return "空"
        }

        return buildString {
            for (i in startAt until size) {
                append(this@joinToString[i]).append(joinRune)
            }
        }.trim().removeSuffix(joinRune)
    }

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
        val current = LocalDateTime.now()

        return java.time.Duration.between(this, current).toKotlinDuration()
    }

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
}
