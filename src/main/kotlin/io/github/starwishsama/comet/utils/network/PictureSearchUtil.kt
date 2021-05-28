/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.utils.network

import cn.hutool.core.util.URLUtil
import cn.hutool.http.ContentType
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.mapper
import io.github.starwishsama.comet.managers.ApiManager
import io.github.starwishsama.comet.objects.config.api.SauceNaoConfig
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
        val key = ApiManager.getConfig<SauceNaoConfig>().token

        NetUtil.executeHttpRequest(
            url = "$sauceNaoApi$encodedUrl${if (key.isNotEmpty()) "&api_key=$key" else ""}",
            timeout = 5
        ).use { response ->
            if (response.isSuccessful && response.isType(ContentType.JSON.value)) {
                val body = response.body?.string() ?: return PicSearchResult.emptyResult()
                try {
                    val resultBody = mapper.readTree(body)
                    if (!resultBody.isNull) {
                        val resultJson = resultBody["results"][0]
                        val similarity = resultJson["header"]["similarity"].asDouble()
                        val pictureUrl = resultJson["header"]["thumbnail"].asText()
                        val originalUrl = resultJson["data"]["ext_urls"][0].asText()
                        return PicSearchResult(pictureUrl, originalUrl, similarity, response.request.url.toString())
                    }
                } catch (e: Exception) {
                    BotVariables.logger.error("[以图搜图] 在解析 API 传回的 json 时出现了问题", e)
                    FileUtil.createErrorReportFile(
                        type = "picsearch", t = e, content = body,
                        message = "Request URL: ${response.request.url}"
                    )
                }
            }
        }
        return PicSearchResult.emptyResult()
    }

    fun ascii2dSearch(url: String): PicSearchResult {
        val request = Jsoup.connect("$ascii2d$url")
        request.header("user-agent", NetUtil.defaultUA).followRedirects(true)
            .apply {
                if (BotVariables.cfg.proxySwitch) {
                    proxy(BotVariables.cfg.proxyUrl, BotVariables.cfg.proxyPort)
                }
            }

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