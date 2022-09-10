package ren.natsuyuk1.comet.pusher.impl.rss

import com.rometools.rome.feed.synd.SyndEntry
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.pusher.CometPushContext
import ren.natsuyuk1.comet.pusher.CometPushTarget

class RSSPusherContext(
    id: String,
    target: List<CometPushTarget>,
    val content: SyndEntry,
): CometPushContext(id, target) {
    override fun normalize(): MessageWrapper =
        buildMessageWrapper {
            appendText(content.title, true)
            appendText(content.description.value, true)
            appendLine()
            appendText("🔗 ${content.uri}")
        }
}