package ren.natsuyuk1.comet.pusher.impl.twitter

import kotlinx.coroutines.runBlocking
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.twitter.toMessageWrapper
import ren.natsuyuk1.comet.pusher.CometPushContext
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.setsuna.objects.TwitterExpansions
import ren.natsuyuk1.setsuna.objects.tweet.Tweet

class TwitterPushContext(
    id: String,
    target: List<CometPushTarget>,
    private val tweet: Tweet,
    private val expansions: TwitterExpansions? = null,
) : CometPushContext(id, target) {
    override fun normalize(): MessageWrapper = runBlocking {
        tweet.toMessageWrapper(expansions)
    }
}
