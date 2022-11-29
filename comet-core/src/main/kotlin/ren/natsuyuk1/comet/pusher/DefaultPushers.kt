package ren.natsuyuk1.comet.pusher

import ren.natsuyuk1.comet.pusher.impl.rss.RSSPusher
import ren.natsuyuk1.comet.pusher.impl.twitter.TwitterPusher

val DEFAULT_PUSHERS = mutableListOf<CometPusher>(
    RSSPusher,
    TwitterPusher,
)
