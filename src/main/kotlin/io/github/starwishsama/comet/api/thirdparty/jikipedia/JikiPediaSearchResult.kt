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
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize

data class JikiPediaSearchResult(
    val url: String,
    val title: String,
    val content: String,
    val date: String,
    val view: String,
    val rateLimit: Boolean = false
) {
    companion object {
        fun empty(rateLimit: Boolean = false): JikiPediaSearchResult {
            return JikiPediaSearchResult("", "", "", "", "", rateLimit)
        }
    }

    fun toMessageWrapper(): MessageWrapper {
        return if (rateLimit) {
            MessageWrapper().setUsable(false)
        } else if (content.isEmpty()) {
            MessageWrapper()
        } else {
            MessageWrapper().addText(
                """
$title
$date | 阅读 $view

${if (content.length > 100) content.limitStringSize(100) + "\n🔗 查看全部 $url" else content}
            """.trimIndent()
            )
        }
    }
}
