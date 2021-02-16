package io.github.starwishsama.comet.utils.network

import cn.hutool.core.util.URLUtil
import cn.hutool.http.ContentType
import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.pojo.PicSearchResult
import io.github.starwishsama.comet.utils.FileUtil
import org.jsoup.Jsoup

/**
 * 图片搜索工具类
 */
object PictureSearchUtil {
    private const val sauceNaoApi = "https://saucenao.com/search.php?db=5&output_type=2&numres=3&url="
    private const val ascii2d = "https://ascii2d.net/search/url/"

    fun sauceNaoSearch(url: String): PicSearchResult {
        val encodedUrl = URLUtil.encode(url)
        val key = BotVariables.cfg.sauceNaoApiKey

        NetUtil.executeHttpRequest(
                url = "$sauceNaoApi$encodedUrl${if (key != null && key.isNotEmpty()) "&api_key=$key" else ""}",
                timeout = 5
        ).use {response ->
            if (response.isSuccessful && response.isType(ContentType.JSON.value)) {
                val body = response.body?.string() ?: return PicSearchResult.emptyResult()
                try {
                    val resultBody = JsonParser.parseString(body)
                    if (resultBody.isJsonObject) {
                        val resultJson = resultBody.asJsonObject["results"].asJsonArray[0].asJsonObject
                        val similarity = resultJson["header"].asJsonObject["similarity"].asDouble
                        val pictureUrl = resultJson["header"].asJsonObject["thumbnail"].asString
                        val originalUrl = resultJson["data"].asJsonObject["ext_urls"].asJsonArray[0].asString
                        return PicSearchResult(pictureUrl, originalUrl, similarity, response.request.url.toString())
                    }
                } catch (e: Exception) {
                    BotVariables.logger.error("[以图搜图] 在解析 API 传回的 json 时出现了问题", e)
                    FileUtil.createErrorReportFile(type = "picsearch", t = e, content = body,
                        message = "Request URL: ${response.request.url}")
                }
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
            imgUrl =
                    "https://ascii2d.net/" + elements.select(".image-box")[1].select("img")[0].attributes()["src"]
            original = sources[0].attributes()["href"]
        } catch (ignored: IndexOutOfBoundsException) {
            return PicSearchResult.emptyResult()
        }
        return PicSearchResult(imgUrl, original, -1.0, "$ascii2d$url")
    }
}