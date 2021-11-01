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
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

enum class IDGuidelineType(val rule: Regex) {
    MINECRAFT(Regex("[a-zA-Z0-9_-]*")), UBISOFT(Regex("[a-zA-Z0-9_.-]*"))
}

object StringUtil {
    fun String.withoutColor() = this.replace("\u001B\\[[;\\d]*m".toRegex(), "")

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

    @OptIn(ExperimentalTime::class)
    fun Duration.toFriendly(maxUnit: DurationUnit = TimeUnit.DAYS, msMode: Boolean = true): String {
        val days = toInt(DurationUnit.DAYS)
        val hours = toInt(DurationUnit.HOURS) % 24
        val minutes = toInt(DurationUnit.MINUTES) % 60
        val seconds = (toInt(DurationUnit.SECONDS) % 60 * 1000) / 1000
        val ms = (toInt(DurationUnit.MILLISECONDS) % 60 * 1000 * 1000) / 1000 / 1000
        return buildString {
            if (days != 0 && maxUnit >= TimeUnit.DAYS) append("${days}天")
            if (hours != 0 && maxUnit >= TimeUnit.HOURS) append("${hours}时")
            if (minutes != 0 && maxUnit >= TimeUnit.MINUTES) append("${minutes}分")
            if (seconds != 0 && maxUnit >= TimeUnit.SECONDS) append("${seconds}秒")
            if (maxUnit >= TimeUnit.MILLISECONDS && msMode) append("${ms}毫秒")
        }
    }

    /**
     * 将字符串转换为消息链
     */
    fun String.convertToChain(): MessageChain {
        return PlainText(this).toMessageChain()
    }

    /**
     * 判断字符串是否为整数
     * @return 是否为整数
     */
    fun String.isNumeric(): Boolean {
        return matches("[-+]?\\d*\\.?\\d+".toRegex()) && !this.contains(".")
    }

    fun String.limitStringSize(size: Int): String {
        return if (this.length <= size) this else substring(0, size) + "..."
    }

    /**
     * 获取该 [LocalDateTime] 距今的时间
     *
     */
    @OptIn(ExperimentalTime::class)
    fun LocalDateTime.getLastingTime(): Duration {
        return java.time.Duration.between(this, LocalDateTime.now()).toKotlinDuration()
    }

    /**
     * 获取该 [LocalDateTime] 距今的时间并转换为友好的字符串
     *
     * @param msMode 是否精准到毫秒
     */
    @OptIn(ExperimentalTime::class)
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
}