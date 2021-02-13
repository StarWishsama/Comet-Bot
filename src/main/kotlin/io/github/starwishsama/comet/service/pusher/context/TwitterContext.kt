package io.github.starwishsama.comet.service.pusher.context

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.twitter.data.Tweet
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class TwitterContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    @SerializedName("custom_status")
    override var status: PushStatus = PushStatus.READY,
    val twitterUserName: String,
    var tweet: Tweet
) : PushContext(pushTarget, retrieveTime, status), Pushable {

    override fun toMessageWrapper(): MessageWrapper {
        val original = tweet.toMessageWrapper()
        return MessageWrapper().addText("${tweet.user.name} 发布了一条推文\n").also {
            it.addElements(original.getMessageContent())
        }
    }

    override fun contentEquals(other: PushContext): Boolean {
        if (other !is TwitterContext) return false

        return tweet.id == other.tweet.id && tweet.postTime == other.tweet.postTime
    }
}

fun Collection<PushContext>.getTwitterContext(userName: String): TwitterContext? {
    val result = this.parallelStream().filter { it is TwitterContext && userName == it.twitterUserName }.findFirst()
    return if (result.isPresent) result.get() as TwitterContext else null
}