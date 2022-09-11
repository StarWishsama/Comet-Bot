package ren.natsuyuk1.comet.pusher.impl.rss

import com.rometools.rome.feed.synd.SyndEntry
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.pusher.CometPushContext
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.comet.utils.time.yyMMddWithTimePattern

class RSSPusherContext(
    id: String,
    target: List<CometPushTarget>,
    val content: SyndEntry,
): CometPushContext(id, target) {
    override fun normalize(): MessageWrapper =
        buildMessageWrapper {
            appendText(content.title, true)
            appendText(content.description.value.simplifyHTML(), true)

            if (content.publishedDate != null) {
                appendText("发布于 ${yyMMddWithTimePattern.format(content.publishedDate.toInstant())}", true)
            }

            appendLine()
            appendText("🔗 ${content.uri}")
        }

    private fun String.simplifyHTML(): String {
        var result = this
        result =
            result.replace("<br />".toRegex(), "\n").replace("<br>".toRegex(), "\n").replace("</p><p>".toRegex(), "\n")
                .replace("	".toRegex(), "")
        while (result.indexOf('<') != -1) {
            val l = result.indexOf('<')
            val r = result.indexOf('>')
            result = result.substring(0, l) + result.substring(r + 1)
        }
        while (result.contains("\n\n")) {
            result = result.replace("\n\n".toRegex(), "\n")
        }

        return result
    }
}
