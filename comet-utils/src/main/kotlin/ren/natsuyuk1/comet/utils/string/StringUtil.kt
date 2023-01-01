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
import cn.hutool.crypto.SecureUtil
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.text.Normalizer
import java.text.Normalizer.Form
import javax.imageio.ImageIO

enum class IDGuidelineType(val rule: Regex) {
    MINECRAFT(Regex("[a-zA-Z0-9_-]*")), UBISOFT(Regex("[a-zA-Z0-9_.-]*"))
}

object StringUtil {
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
     * 判断字符串是否为整数
     * @return 是否为整数
     */
    fun String.isNumeric(): Boolean {
        return matches(numberRegex) && !this.contains(".")
    }

    fun String.limit(size: Int): String {
        return if (this.length <= size) this else substring(0, size) + "..."
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

    fun String.replaceAllToBlank(toReplace: List<String>): String {
        var result = this

        toReplace.forEach {
            result = result.replace(it, "")
        }

        return result
    }
}

fun String.replaceWithOrder(vararg args: Any?): String {
    if (args.isEmpty() || isEmpty()) {
        return this
    }
    val chars = toCharArray()
    val builder = StringBuilder(length)
    var i = 0
    while (i < chars.size) {
        val mark = i
        if (chars[i] == '{') {
            var num = 0
            while (i + 1 < chars.size && Character.isDigit(chars[i + 1])) {
                i++
                num *= 10
                num += chars[i] - '0'
            }
            if (i != mark && i + 1 < chars.size && chars[i + 1] == '}') {
                i++
                builder.append(args.getOrNull(num)?.toString() ?: "{$num}")
            } else {
                i = mark
            }
        }
        if (mark == i) {
            builder.append(chars[i])
        }
        i++
    }
    return builder.toString()
}

fun String.toHMAC(key: String): String {
    return SecureUtil.hmacSha256(key).digestHex(this)
}

fun String?.blankIfNull() = if (this.isNullOrEmpty()) "" else this

fun String.normalize(form: Form) = Normalizer.normalize(this, form)
