package ren.natsuyuk1.comet.network.thirdparty.twitter

import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.message.Image
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.setsuna.objects.tweet.ReferencedTweetType
import ren.natsuyuk1.setsuna.response.TweetFetchResponse
import ren.natsuyuk1.setsuna.util.removeShortLink

suspend fun TweetFetchResponse.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        val tweet = this@toMessageWrapper.tweet!!
        val author = tweet.authorID?.let { TwitterAPI.fetchUser(it) }
        appendText("${author?.name} | @${author?.username}", true)
        appendText(tweet.text.removeShortLink().limit(100), true)
        appendLine()
        if (tweet.referencedTweets != null) {
            val rtInfo = tweet.referencedTweets?.firstOrNull()
            if (rtInfo != null) {
                val rt = TwitterAPI.fetchTweet(rtInfo.id)
                val rtTweet = rt?.tweet
                val rtAuthor = rtTweet?.authorID?.let { TwitterAPI.fetchUser(it) }

                if (rtTweet != null && rtAuthor != null) {
                    when (rtInfo.type) {
                        ReferencedTweetType.RETWEETED -> appendText("\uD83D\uDD01 转发了 ${rtAuthor.name.limit(15)} 的推文", true)
                        ReferencedTweetType.REPLY_TO -> appendText("\uD83D\uDCAC 回复了 ${rtAuthor.name.limit(15)} 的推文", true)
                        ReferencedTweetType.QUOTED -> appendText("\uD83D\uDCAC 引用了 ${rtAuthor.name.limit(15)} 的推文", true)
                        else -> {}
                    }

                    appendText(rtTweet.text.removeShortLink().limit(50), true)
                    appendLine()

                    if (rt.includes?.media?.isEmpty() == false) {
                        rt.includes?.media?.take(2)?.forEach {
                            appendElement(Image(url = it.url!!))
                        }

                        appendLine()
                    }
                }
            }
        }

        if (includes?.media?.isEmpty() == false) {
            includes?.media?.take(2)?.forEach {
                appendElement(Image(url = it.url!!))
            }

            appendLine()
        }

        if (tweet.publicMetrics != null) {
            val metrics = tweet.publicMetrics!!
            appendText("💬 ${metrics.reply.getBetterNumber()} \uD83D\uDD01 ${metrics.retweet.getBetterNumber()} 👍 ${metrics.like.getBetterNumber()}")
        }
    }
