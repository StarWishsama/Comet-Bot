package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.objects.pojo.twitter.Tweet
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class TwitterContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    override var status: PushStatus = PushStatus.READY,
    val twitterUserName: String,
    var tweet: Tweet
) : PushContext(pushTarget, retrieveTime, status) {

    override fun toMessageWrapper(): MessageWrapper {
        val original = tweet.toMessageWrapper()
        return MessageWrapper("${tweet.user.name} 发布了一条推文\n" + original.text).also {
            it.pictureUrl.addAll(original.pictureUrl)
        }
    }

    override fun compareTo(other: PushContext): Boolean {
        if (other !is TwitterContext) return false

        return tweet.postTime == other.tweet.postTime
    }
}

fun Collection<TwitterContext>.getContext(userName: String): TwitterContext? {
    val result = this.parallelStream().filter { userName == it.twitterUserName }.findFirst()
    return if (result.isPresent) result.get() else null
}