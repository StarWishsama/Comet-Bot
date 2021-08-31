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
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.utils.network.NetUtil
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException
import java.nio.charset.StandardCharsets

object JikiPediaApi : ApiExecutor {
    private const val searchRoute = "https://jikipedia.com/search?phrase="

    fun search(keyword: String): JikiPediaSearchResult {
        return try {
            if (isReachLimit()) {
                return JikiPediaSearchResult.empty(true)
            }

            usedTime++

            val connection = Jsoup.connect(searchRoute + URLEncoder.DEFAULT.encode(keyword, StandardCharsets.UTF_8))

            connection.userAgent(NetUtil.defaultUA)

            if (CometVariables.cfg.proxySwitch) {
                connection.proxy(CometVariables.cfg.proxyUrl, CometVariables.cfg.proxyPort)
            }

            val document = connection.get()

            val tile = document.selectFirst("#search > div > div.masonry")
                ?.getElementsByClass("tile")?.get(0)

            val render = tile?.getElementsByClass("brax-render")

            val content = render?.first()?.allElements?.let { concatContent(it) }

            if (content == null) {
                JikiPediaSearchResult.empty()
            } else {
                return JikiPediaSearchResult(keyword, content)
            }
        } catch (e: IOException) {
            JikiPediaSearchResult.empty()
        }
    }

    private fun concatContent(elements: Elements): String {
        return if (elements.isNotEmpty()) {
            buildString {
                elements.forEach { ele ->
                    if (ele.className() == "text") {
                        append(ele.text())
                    }
                }
            }
        } else {
            ""
        }
    }

    override var usedTime: Int = 0

    override val duration: Int = 3

    override fun getLimitTime(): Int = 20
}