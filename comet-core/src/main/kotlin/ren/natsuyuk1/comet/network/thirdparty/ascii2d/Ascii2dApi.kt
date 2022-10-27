package ren.natsuyuk1.comet.network.thirdparty.ascii2d

import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup
import ren.natsuyuk1.comet.consts.cometClient

object Ascii2dApi {
    private const val API_ROUTE = "https://ascii2d.net/search/url/[url]?type=color"

    suspend fun searchImage(url: String) {
        val as2 = API_ROUTE.replace("[url]", url)
        println(as2)
        val req = cometClient.client.get(as2)
        println(req.request.url)
        println(req.headers)
        println("Code is ${req.status}")
        val body = req.bodyAsText()
        val doc = Jsoup.parse(body)
        val ele = doc.select("div.row:nth-child(6)")

        println(ele)
    }
}
