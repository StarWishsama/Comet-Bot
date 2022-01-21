/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.jikipedia

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import io.ktor.http.*

data class JikiPediaSearchResult(
    val url: String,
    val title: String,
    val content: String,
    val date: String,
    val view: String,
    val responseCode: Int = 200
) {
    companion object {
        fun empty(responseCode: Int): JikiPediaSearchResult {
            return JikiPediaSearchResult("", "", "", "", "", responseCode)
        }
    }

    fun toMessageWrapper(): MessageWrapper {
        return if (responseCode != 200) {
            when (responseCode) {
                HttpStatusCode.NotFound.value -> MessageWrapper().addText("找不到对应的结果")
                HttpStatusCode.Unauthorized.value -> MessageWrapper().addText("访问过于频繁，请登陆后重试. 请联系管理员")
                else -> MessageWrapper().addText("已达到小鸡百科搜索上限, 请稍后再尝试 | $responseCode")
            }
        } else if (content.isEmpty()) {
            MessageWrapper().addText("找不到搜索结果")
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
