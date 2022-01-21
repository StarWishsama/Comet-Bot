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

import io.github.starwishsama.comet.CometVariables
import net.mamoe.mirai.message.data.*
import java.util.*

fun MessageChain.doFilter(): MessageChain {
    if (CometVariables.cfg.filterWords.isNullOrEmpty()) {
        return this
    }

    val revampChain = LinkedList<SingleMessage>()
    this.forEach { revampChain.add(it) }

    for (i in revampChain.indices) {
        if (revampChain[i] is PlainText) {
            var context = revampChain[i].content
            CometVariables.cfg.filterWords.forEach {
                if (context.contains(it)) {
                    var replaceText = ""
                    repeat(it.length) {
                        replaceText += "*"
                    }
                    context = context.replace(it.toRegex(), replaceText)
                }
            }
            revampChain[i] = PlainText(context)
        }
    }

    return revampChain.toMessageChain()
}