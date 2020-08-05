package io.github.starwishsama.comet.utils

import cn.hutool.core.util.URLUtil
import cn.hutool.http.ContentType
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.pojo.PicSearchResult
import org.jsoup.Jsoup

object PictureSearchUtil {
    private const val sauceNaoApi = "https://saucenao.com/search.php?db=5&output_type=2&numres=3&url="
    private const val ascii2d = "https://ascii2d.net/search/url/"

    fun sauceNaoSearch(url: String): PicSearchResult {
        val encodedUrl = URLUtil.encode(url)
        val key = BotVariables.cfg.sauceNaoApiKey
        val request = NetUtil.doHttpRequestGet(
            "$sauceNaoApi$encodedUrl${if (key != null && key.isNotEmpty()) "&api_key=$key" else ""}",
            5000
        )
        val result = request.executeAsync()

        if (result.isOk && result.header("Content-Type").contains(ContentType.JSON.value)) {
            val body = result.body()
            try {
                val resultBody = JsonParser.parseString(body)
                if (resultBody.isJsonObject) {
                    val resultJson = resultBody.asJsonObject["results"].asJsonArray[0].asJsonObject
                    val similarity = resultJson["header"].asJsonObject["similarity"].asDouble
                    val pictureUrl = resultJson["header"].asJsonObject["thumbnail"].asString
                    val originalUrl = resultJson["data"].asJsonObject["ext_urls"].asJsonArray[0].asString
                    return PicSearchResult(pictureUrl, originalUrl, similarity, request.url)
                }
            } catch (e: Exception) {
                BotVariables.logger.error("[以图搜图] 在解析 API 传回的 json 时出现了问题", e)
                FileUtil.createErrorReportFile("picsearch", e, body, request.url)
            }
        }
        return PicSearchResult.emptyResult()
    }

    fun ascii2dSearch(url: String): PicSearchResult {
        val request = Jsoup.connect("$ascii2d$url")
        request.header("user-agent", NetUtil.defaultUA).followRedirects(true)
            .proxy(BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort)
        println("$ascii2d$url")

        val html = request.get()
        val elements = html.body().getElementsByClass("container")
        val imgUrl: String
        val sources = elements.select(".info-box")[1].select("a")
        val original: String
        try {
            imgUrl = "https://ascii2d.net/" + elements.select(".image-box")[1].select("img")[0].attributes()["src"]
            original = sources[0].attributes()["href"]
        } catch (ignored: IndexOutOfBoundsException) {
            return PicSearchResult.emptyResult()
        }
        return PicSearchResult(imgUrl, original, -1.0, "$ascii2d$url")
    }
}