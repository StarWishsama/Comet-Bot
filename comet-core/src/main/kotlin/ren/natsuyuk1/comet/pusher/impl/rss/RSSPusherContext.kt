package ren.natsuyuk1.comet.pusher.impl.rss

import com.rometools.rome.feed.synd.SyndFeed
import ren.natsuyuk1.comet.pusher.CometPushContext
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.comet.utils.message.MessageWrapper

class RSSPusherContext(
    id: String,
    target: List<CometPushTarget>,
    content: SyndFeed,
): CometPushContext(id, target) {
    override fun normalize(): MessageWrapper {
        TODO("Not yet implemented")
    }
}
