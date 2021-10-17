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
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.api.thirdparty.ApiExecutor
import io.github.starwishsama.comet.utils.network.NetUtil
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException
import java.nio.charset.StandardCharsets

object JikiPediaApi : ApiExecutor {
    private const val searchRoute = "https://jikipedia.com/search?phrase="

    fun searchByKeyWord(keyword: String): JikiPediaSearchResult {
        return try {
            if (isReachLimit()) {
                return JikiPediaSearchResult.empty(HttpStatusCode.TooManyRequests.value)
            }

            usedTime++

            // 搜索内容

            val id = searchID(keyword)

            if (id.first.isEmpty()) {
                return JikiPediaSearchResult.empty(id.second)
            }

            // 详细内容

            runBlocking { delay(1_500) }

            return parseDefinition("https://jikipedia.com/definition/${id.first}")
        } catch (e: IOException) {
            daemonLogger.warning(e)
            JikiPediaSearchResult.empty(HttpStatusCode.InternalServerError.value)
        }
    }

    private fun searchID(keyword: String): Pair<String, Int> {
        val connection = Jsoup.connect(searchRoute + URLEncoder.DEFAULT.encode(keyword, StandardCharsets.UTF_8))

        connection.userAgent(NetUtil.defaultUA)

        if (CometVariables.cfg.proxySwitch) {
            connection.proxy(CometVariables.cfg.proxyUrl, CometVariables.cfg.proxyPort)
        }

        val resp = connection.execute()

        if (CometVariables.cfg.debugMode) {
            daemonLogger.debug("JikiPedia incoming body:")
            daemonLogger.debug(resp.body())
        }

        if (resp.statusCode() != HttpStatusCode.OK.value) {
            return Pair("", resp.statusCode())
        }

        val document = resp.parse()

        val tile = document.selectFirst("#search > div > div.masonry")
            ?.getElementsByClass("tile")?.get(0)

        return Pair(tile?.attr("data-id") ?: "", resp.statusCode())
    }

    private fun parseDefinition(url: String): JikiPediaSearchResult {
        val detailedConnection = Jsoup.connect(url)

        detailedConnection.userAgent(NetUtil.defaultUA)

        if (CometVariables.cfg.proxySwitch) {
            detailedConnection.proxy(CometVariables.cfg.proxyUrl, CometVariables.cfg.proxyPort)
        }

        val detailedResp = detailedConnection.execute()

        if (CometVariables.cfg.debugMode) {
            daemonLogger.debug("JikiPedia incoming body:")
            daemonLogger.debug(detailedResp.body())
        }

        if (detailedResp.statusCode() != HttpStatusCode.OK.value) {
            return JikiPediaSearchResult.empty(detailedResp.statusCode())
        }

        val detailedDocument = detailedResp.parse()

        /**
         * Hello moss
         *
         * 访问过于频繁，请登陆后重试
         * 北京市第三交通委提醒您：
         * 道路千万条，安全第一条。
         * 行车不规范，亲人两行泪。
         */
        if (detailedDocument.title().contains("moss")) {
            return JikiPediaSearchResult.empty(HttpStatusCode.Unauthorized.value)
        }

        val title = detailedDocument.getElementsByClass("title")[0]?.text() ?: "获取失败"

        val dateAndView = detailedDocument.getElementsByClass("basic-info-rela")

        val date = if (dateAndView.isNotEmpty()) dateAndView[0]?.getElementsByClass("created")?.get(0)?.text()
            ?: "未知" else "获取失败"

        val view = if (dateAndView.isNotEmpty()) dateAndView[0]?.getElementsByClass("view-container")?.get(0)
            ?.getElementsByClass("view")?.get(0)?.text() ?: "未知" else "获取失败"

        val render = detailedDocument.getElementsByClass("brax-render")

        val content = render.first()?.allElements?.let { concatContent(it) }

        return if (content == null) {
            JikiPediaSearchResult.empty(detailedResp.statusCode())
        } else {
            JikiPediaSearchResult(url, title, content, date, view)
        }
    }

    private fun concatContent(elements: Elements): String {
        return if (elements.isNotEmpty()) {
            buildString {
                elements.forEach { ele ->
                    if (ele.className().contains("text") || ele.className() == "highlight") {
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