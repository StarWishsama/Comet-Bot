package io.github.starwishsama.comet.utils

import com.google.gson.JsonParser
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.pojo.PicSearchResult

object PictureSearchUtil {
    private const val apiUrl = "https://saucenao.com/search.php?db=999&output_type=2&numres=3&url="

    fun sauceNaoSearch(url: String): PicSearchResult {
        val request = NetUtil.doHttpRequestGet(apiUrl + url, 5000)
        val result = request.executeAsync()

        if (result.isOk) {
            val body = result.body()
            try {
                val resultBody = JsonParser.parseString(body)
                if (resultBody.isJsonObject) {
                    val resultJson = resultBody.asJsonObject["results"].asJsonArray[0].asJsonObject
                    val similarity = resultJson["header"].asJsonObject["similarity"].asDouble
                    val pictureUrl = resultJson["header"].asJsonObject["thumbnail"].asString
                    val originalUrl = resultJson["data"].asJsonObject["ext_urls"].asJsonArray[0].asString
                    return PicSearchResult(pictureUrl, originalUrl, similarity)
                }
            } catch (e: Exception) {
                BotVariables.logger.error("[以图搜图] 在解析 API 传回的 json 时出现了问题", e)
                FileUtil.createErrorReportFile("picsearch", e, body, request.url)
            }
        }
        return PicSearchResult.emptyResult()
    }
}