package ren.natsuyuk1.comet.pusher

import ren.natsuyuk1.comet.pusher.impl.rss.RSSPusher

val DEFAULT_PUSHERS = mutableListOf<CometPusher>(
    RSSPusher
)
