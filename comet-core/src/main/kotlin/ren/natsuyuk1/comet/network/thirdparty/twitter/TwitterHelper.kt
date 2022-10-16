package ren.natsuyuk1.comet.network.thirdparty.twitter

import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.asURLImage
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.setsuna.objects.TwitterExpansions
import ren.natsuyuk1.setsuna.objects.tweet.ReferencedTweetType
import ren.natsuyuk1.setsuna.objects.tweet.Tweet
import ren.natsuyuk1.setsuna.objects.user.TwitterUser
import ren.natsuyuk1.setsuna.util.removeShortLink

suspend fun Tweet.toMessageWrapper(includes: TwitterExpansions? = null): MessageWrapper =
    buildMessageWrapper {
        val tweet = this@toMessageWrapper
        val author = tweet.authorID?.let { TwitterAPI.fetchUser(it) }
        appendTextln("${author?.name} | @${author?.username}")
        appendTextln(tweet.text.removeShortLink().limit(100))
        if (tweet.referencedTweets != null) {
            val rtInfo = tweet.referencedTweets?.firstOrNull()
            if (rtInfo != null) {
                val rt = TwitterAPI.fetchTweet(rtInfo.id)
                val rtTweet = rt?.tweet
                val rtAuthor = rtTweet?.authorID?.let { TwitterAPI.fetchUser(it) }

                if (rtTweet != null && rtAuthor != null) {
                    appendLine()

                    when (rtInfo.type) {
                        ReferencedTweetType.RETWEETED ->
                            appendTextln("\uD83D\uDD01 转发了 ${rtAuthor.name.limit(15)} 的推文")
                        ReferencedTweetType.REPLY_TO ->
                            appendTextln("\uD83D\uDCAC 回复了 ${rtAuthor.name.limit(15)} 的推文")
                        ReferencedTweetType.QUOTED ->
                            appendTextln("\uD83D\uDCAC 引用了 ${rtAuthor.name.limit(15)} 的推文")
                        else -> {}
                    }

                    appendTextln(rtTweet.text.removeShortLink().limit(50))
                    appendLine()

                    if (rt.includes?.media?.isEmpty() == false) {
                        rt.includes?.media?.take(2)?.forEach {
                            it.url?.let { url ->
                                appendElement(url.asURLImage())
                            }
                        }

                        appendLine()
                    }
                }
            }
        }

        if (includes?.media?.isEmpty() == false) {
            includes.media?.take(2)?.forEach {
                it.url?.let { url ->
                    appendElement(url.asURLImage())
                }
            }

            appendLine()
        }

        if (tweet.publicMetrics != null) {
            appendLine()
            val metrics = tweet.publicMetrics!!
            val reply = metrics.reply.getBetterNumber()
            val retweet = metrics.retweet.getBetterNumber()
            val like = metrics.like.getBetterNumber()
            appendText("💬 $reply \uD83D\uDD01 $retweet 👍 $like")
        }
    }

fun TwitterUser.toMessageWrapper() = buildMessageWrapper {
    appendElement(profileImageURL!!.asURLImage())
    appendTextln("$name (@$username)")

    if (bio != null) {
        appendLine()
        appendText(bio!!.limit(50))
        appendLine()
    }

    if (location != null) {
        appendText("📍 $location")
        if (url != null) {
            appendText(" 🔗 $url")
        }

        appendLine()
    }

    appendTextln("${publicMetrics?.following} 正在关注 | ${publicMetrics?.followers} 关注者")
}
