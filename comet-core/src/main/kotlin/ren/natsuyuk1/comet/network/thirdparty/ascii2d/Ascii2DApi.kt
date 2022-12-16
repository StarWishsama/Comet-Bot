package ren.natsuyuk1.comet.network.thirdparty.ascii2d

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mu.KotlinLogging
import org.jsoup.Jsoup
import ren.natsuyuk1.comet.api.message.Image
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asURLImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.consts.cometClient

private val logger = KotlinLogging.logger {}

object Ascii2DApi {
    suspend fun searchByImage(image: Image, picMode: Boolean = true): MessageWrapper {
        require(!image.url.isNullOrBlank()) { "Image for ascii2d search must not be empty!" }

        val resp = cometClient.client.config {
            followRedirects = true
        }.post("https://ascii2d.net/search/uri") {
            headers["User-Agent"] = "PostmanRuntime/7.30.0"
            setBody("""{"uri": "${image.url}"}""")
            contentType(ContentType.Application.Json)
        }

        val redir = cometClient.client.get(resp.headers["location"]!!) {
            headers["User-Agent"] = "PostmanRuntime/7.30.0"
            headers {
                append("set-cookie", resp.headers["set-cookie"]!!)
                append("x-request-id", resp.headers["x-request-id"]!!)
                append("Report-To", resp.headers["Report-To"]!!)
            }
        }

        val document = Jsoup.parse(redir.bodyAsText())

        val elements = document.body().getElementsByClass("container")
        val imgUrl: String
        val sources = elements.select(".info-box")[1].select("a")
        val original: String
        try {
            imgUrl =
                "https://ascii2d.net/" + elements.select(".image-box")[1].select("img")[0].attributes()["src"]
            original = sources[0].attributes()["href"]
        } catch (ignored: IndexOutOfBoundsException) {
            return buildMessageWrapper {
                appendText("无法找到该图片的以图搜图结果.")
            }
        }

        return buildMessageWrapper {
            appendTextln("✔ 已找到可能的图片来源")
            appendLine()
            appendText("🔗 源地址 $original")
            if (picMode) {
                appendTextln("")
                appendElement(imgUrl.asURLImage())
            }
        }
    }
}
