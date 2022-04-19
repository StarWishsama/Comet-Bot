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

import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.utils.StringUtil.removeTrailingNewline
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage

fun Sequence<SingleMessage>.doFilter(): Sequence<SingleMessage> {
    if (cfg.filterWords.isEmpty()) return this

    return fold(sequenceOf()) { chain, single ->
        chain.plus(
            if (single is PlainText) {
                PlainText(single.content.filterWords())
            } else single
        )
    }
}

private fun String.filterWords() =
    cfg.filterWords.fold(this) { acc, i ->
        if (acc.contains(i)) {
            val replaceText = "*".repeat(i.length)
            acc.replace(i.toRegex(), replaceText)
        } else acc
    }

fun Sequence<SingleMessage>.removeTrailingNewline(): Sequence<SingleMessage> =
    fold(sequenceOf()) { acc, i ->
        acc.plus(
            if (i is PlainText) {
                PlainText(i.content.removeTrailingNewline())
            } else i
        )
    }
