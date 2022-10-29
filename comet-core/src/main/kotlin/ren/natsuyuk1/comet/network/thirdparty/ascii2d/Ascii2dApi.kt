package ren.natsuyuk1.comet.network.thirdparty.ascii2d

import io.ktor.client.request.*
import io.ktor.client.statement.*
import mu.KotlinLogging
import org.jsoup.Jsoup
import ren.natsuyuk1.comet.consts.cometClient

private val logger = KotlinLogging.logger {}

object Ascii2dApi {
    private const val API_ROUTE = "https://ascii2d.net/search/url/[url]?type=color"

    suspend fun searchImage(url: String): Ascii2dSearchResult {
        val reqURL = API_ROUTE.replace("[url]", url)
        return try {
            val req = cometClient.client.get(reqURL)
            val doc = Jsoup.parse(req.bodyAsText())
            val elements = doc.body().getElementsByClass("container")
            val infoBox = elements.select(".info-box")

            if (infoBox.isEmpty()) {
                return Ascii2dSearchResult("", "", "❌ 找不到该图片的搜索结果")
            }

            val source = infoBox[1].select("a")

            val originURL = source[0].attributes()["href"]
            val authorName = source[1].childNode(0).attributes().first().value

            Ascii2dSearchResult(authorName, originURL)
        } catch (e: Exception) {
            logger.warn(e) { "Unable to fetch ascii2d search data, url: $reqURL" }
            Ascii2dSearchResult("", "", "❌ 在搜索时遇到了问题")
        }
    }
}
