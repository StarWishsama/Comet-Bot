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

import cn.hutool.core.net.URLEncoder
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException
import java.nio.charset.StandardCharsets

object JikiPediaApi {
    private const val searchRoute = "https://jikipedia.com/search?phrase="

    fun search(keyword: String): JikiPediaSearchResult {
        return try {
            println(searchRoute + URLEncoder.DEFAULT.encode(keyword, StandardCharsets.UTF_8))
            val document = Jsoup.connect(searchRoute + URLEncoder.DEFAULT.encode(keyword, StandardCharsets.UTF_8)).get()

            val firstResult = document.selectFirst("#search > div > div.masonry")
                ?.getElementsByAttributeValue("data-index", "0")

            val render = firstResult?.first()?.getElementsByClass("brax-render")

            //println(render)

            println(render?.let { concatContent(it) })

            JikiPediaSearchResult.empty()

        } catch (e: IOException) {
            JikiPediaSearchResult.empty()
        }
    }

    private fun concatContent(elements: Elements): String {
        return if (elements.isNotEmpty()) {
            buildString {
                elements.forEach { ele ->
                    println(ele.allElements)
                    val result = ele.allElements.first()?.data()
                    //println(result)
                    append(result)
                }
            }
        } else {
            ""
        }
    }
}