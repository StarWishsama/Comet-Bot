/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.jikipedia

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

data class JikiPediaSearchResult(
    val title: String,
    val content: String,
    val rateLimit: Boolean = false
) {
    companion object {
        fun empty(rateLimit: Boolean = false): JikiPediaSearchResult {
            return JikiPediaSearchResult("", "", rateLimit)
        }
    }

    fun toMessageWrapper(): MessageWrapper {
        return if (rateLimit) {
            MessageWrapper().setUsable(false)
        } else if (content.isEmpty()) {
            MessageWrapper()
        } else {
            MessageWrapper().addText("搜索 $title 为你找到以下可能解释：\n$content")
        }
    }
}
