/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.noabbr.data

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class AbbrSearchResponse(
    val name: String = "",
    val inputting: List<String> = mutableListOf(),
    val trans: List<String> = mutableListOf()
) {
    fun isEmpty(): Boolean = this.name.isEmpty() || this.trans.isEmpty()

    companion object {
        val empty: AbbrSearchResponse = AbbrSearchResponse()
    }

    fun toMessageWrapper(): MessageWrapper {
        if (name.isEmpty() || this == empty) {
            return MessageWrapper().addText("找不到对应结果")
        }

        if (trans.isEmpty() && inputting.isNotEmpty()) {
            val result = MessageWrapper().addText(
                "你输入的可能是: ${inputting.subList(0, inputting.size.coerceAtMost(5))}\n"
            )

            if (inputting.size > 5) {
                result.addText("仅显示前五条可能结果")
            }

            return result
        }

        if (trans.isNotEmpty()) {
            val result = MessageWrapper().addText(
                "缩写对应的可能结果: ${trans.subList(0, trans.size.coerceAtMost(5))}\n"
            )

            if (inputting.size > 5) {
                result.addText("仅显示前五条可能结果")
            }

            return result
        }

        return MessageWrapper().addText("找不到对应结果")
    }
}