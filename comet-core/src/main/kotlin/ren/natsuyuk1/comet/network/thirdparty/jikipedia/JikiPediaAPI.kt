package ren.natsuyuk1.comet.network.thirdparty.jikipedia

import cn.hutool.core.net.RFC3986.UNRESERVED
import io.ktor.http.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import ren.natsuyuk1.comet.api.config.CometConfig
import java.io.IOException
import java.nio.charset.StandardCharsets

private val logger = mu.KotlinLogging.logger {}

object JikiPediaAPI {
    private const val searchRoute = "https://jikipedia.com/search?phrase="
    private var reachLimit by atomic(false)

    fun searchByKeyWord(keyword: String): JikiPediaSearchResult {
        return try {
            // 搜索内容
            val id = searchID(keyword)

            if (id.first.isEmpty()) {
                return JikiPediaSearchResult.empty(id.second)
            }

            // 详细内容
            runBlocking { delay(1_500) }
            return parseDefinition("https://jikipedia.com/definition/${id.first}")
        } catch (e: IOException) {
            logger.warn(e) { "在搜索小鸡百科内容时出现了问题" }
            reachLimit = true
            JikiPediaSearchResult.empty(HttpStatusCode.InternalServerError.value)
        }
    }

    private fun searchID(keyword: String): Pair<String, Int> {
        val connection = Jsoup.connect(searchRoute + UNRESERVED.encode(keyword, StandardCharsets.UTF_8))

        connection.userAgent(CometConfig.data.useragent)

        val resp = connection.execute()
        val document = resp.parse()

        /**
         * Hello moss
         *
         * 访问过于频繁，请登陆后重试
         * 北京市第三交通委提醒您：
         * 道路千万条，安全第一条。
         * 行车不规范，亲人两行泪。
         */
        if (document.title().contains("moss")) {
            return Pair("", HttpStatusCode.Unauthorized.value)
        }

        if (resp.statusCode() != HttpStatusCode.OK.value) {
            return Pair("", resp.statusCode())
        }

        val tile = document.selectFirst("#search > div > div.masonry")
            ?.getElementsByClass("tile")?.get(0)

        return Pair(tile?.attr("data-id") ?: "", resp.statusCode())
    }

    private fun parseDefinition(url: String): JikiPediaSearchResult {
        val detailedConnection = Jsoup.connect(url)

        detailedConnection.userAgent(CometConfig.data.useragent)

        val detailedResp = detailedConnection.execute()

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
}