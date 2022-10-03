package ren.natsuyuk1.comet.pusher.impl.twitter

import ren.natsuyuk1.comet.pusher.CometPusher
import ren.natsuyuk1.comet.pusher.CometPusherConfig

class TwitterPusher : CometPusher("twitter", CometPusherConfig(300)) {
    override suspend fun retrieve() {
        TODO("Not yet implemented")
    }
}
