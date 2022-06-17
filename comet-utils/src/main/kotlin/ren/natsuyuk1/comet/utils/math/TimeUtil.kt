/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.utils.math

import ren.natsuyuk1.comet.utils.string.StringUtil.isNumeric

object TimeUtil {
    fun parseTextTime(message: String): Int {
        if (message.isNumeric()) {
            return message.toInt()
        }

        var totalTime: Long

        val yearRegex = Regex("""(\d{1,2})[yY年]""")
        val monRegex = Regex("""(\d{1,2})(月|mon|Mon)""")
        val dayRegex = Regex("""(\d{1,2})[dD天]""")
        val hourRegex = Regex("""(\d{1,2})(小时|时|h|H)""")
        val minRegex = Regex("""(\d{1,2})(分钟|分|m|M)(?!on)""")
        val secRegex = Regex("""(\d{1,7})(秒钟|秒|s|S)""")

        totalTime = yearRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(365 * 24 * 60 * 60) ?: 0L
        totalTime += monRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(30 * 24 * 60 * 60) ?: 0L
        totalTime += dayRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(24 * 60 * 60) ?: 0L
        totalTime += hourRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(60 * 60) ?: 0L
        totalTime += minRegex.find(message)?.groups?.get(1)?.value?.toLong()?.times(60) ?: 0L
        totalTime += secRegex.find(message)?.groups?.get(1)?.value?.toLong() ?: 0L

        return totalTime.toInt()
    }
}
