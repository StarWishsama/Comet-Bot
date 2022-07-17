package ren.natsuyuk1.comet.network.thirdparty.jikipedia

import io.ktor.http.*
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit

data class JikiPediaSearchResult(
    val url: String,
    val title: String,
    val content: String,
    val date: String,
    val view: String,
    val responseCode: Int = 200
) {
    companion object {
        fun empty(responseCode: Int): JikiPediaSearchResult {
            return JikiPediaSearchResult("", "", "", "", "", responseCode)
        }
    }

    fun toMessageWrapper(): MessageWrapper {
        return if (responseCode != 200) {
            when (responseCode) {
                HttpStatusCode.NotFound.value -> buildMessageWrapper { appendText("找不到对应的结果") }
                HttpStatusCode.Unauthorized.value -> buildMessageWrapper { appendText("访问过于频繁，请登陆后重试. 请联系管理员") }
                else -> buildMessageWrapper { appendText("已达到小鸡百科搜索上限, 请稍后再尝试 | $responseCode") }
            }
        } else if (content.isEmpty()) {
            buildMessageWrapper { appendText("找不到搜索结果") }
        } else {
            buildMessageWrapper {
                appendText(
                    """
$title
$date | 阅读 $view
${if (content.length > 100) content.limit(100) + "\n🔗 查看全部 $url" else content}
            """.trimIndent()
                )
            }
        }
    }
}
