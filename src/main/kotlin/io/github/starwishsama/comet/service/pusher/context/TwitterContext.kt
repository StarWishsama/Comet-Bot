package io.github.starwishsama.comet.service.pusher.context

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class TwitterContext(
    pushTarget: MutableList<Long> = mutableListOf(),
    retrieveTime: Long,
    @SerializedName("custom_status")
    override var status: PushStatus = PushStatus.READY,
    val twitterUserName: String,
    var tweetId: Long
) : PushContext(pushTarget, retrieveTime, status), Pushable {

    override fun toMessageWrapper(): MessageWrapper {
        val tweet = TwitterApi.getCacheByID(tweetId) ?: return MessageWrapper().setUsable(false)
        val original = tweet.toMessageWrapper()
        return MessageWrapper().addText("${tweet.user.name} 发布了一条推文\n").also {
            it.addElements(original.getMessageContent())
        }
    }

    override fun contentEquals(other: PushContext): Boolean {
        if (other !is TwitterContext) return false

        return tweetId == other.tweetId
    }
}

fun Collection<TwitterContext>.getTwitterContext(userName: String): TwitterContext? {
    for (tc in this) {
        if (tc.twitterUserName == userName) {
            return tc
        }
    }
    return null
}