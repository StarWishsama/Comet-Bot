/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.noabbr.data

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class AbbrSearchResponse(
    val name: String = "",
    val trans: List<String> = mutableListOf()
) {
    fun isEmpty(): Boolean = this.name.isEmpty() || this.trans.isEmpty()

    companion object {
        val empty: AbbrSearchResponse = AbbrSearchResponse()
    }

    fun toMessageWrapper(): MessageWrapper {
        if (isEmpty() || this == empty) {
            return MessageWrapper().addText("找不到对应结果")
        }

        val result = MessageWrapper().addText(
            """
                🔍 $name 对应的可能结果 >
                ${
                buildString {
                    trans.subList(0, trans.size.coerceAtMost(5)).forEach { append("${it}, ") }
                }.removeSuffix(", ")
            }
                """.trimIndent()
        )

        if (trans.size > 5) {
            result.addText("\n💡 仅显示前五条可能结果")
        }

        return result
    }
}