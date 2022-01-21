/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.utils

import org.fusesource.jansi.Ansi

object AnsiUtil {
    enum class Color(private val format: String) {
        RESET("\u001b[0m"),

        WHITE("\u001b[30m"),
        RED("\u001b[31m"),
        EMERALD_GREEN("\u001b[32m"),
        GOLD("\u001b[33m"),
        BLUE("\u001b[34m"),
        PURPLE("\u001b[35m"),
        GREEN("\u001b[36m"),

        GRAY("\u001b[90m"),
        LIGHT_RED("\u001b[91m"),
        LIGHT_GREEN("\u001b[92m"),
        LIGHT_YELLOW("\u001b[93m"),
        LIGHT_BLUE("\u001b[94m"),
        LIGHT_PURPLE("\u001b[95m"),
        LIGHT_CYAN("\u001b[96m"),

        DARK_RED(Ansi.ansi().fgRgb(170, 0, 0).toString());

        override fun toString(): String = format
    }
}