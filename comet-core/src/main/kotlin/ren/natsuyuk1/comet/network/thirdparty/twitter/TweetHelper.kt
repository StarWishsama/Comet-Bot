package ren.natsuyuk1.comet.network.thirdparty.twitter

import ren.natsuyuk1.comet.utils.math.NumberUtil.getBetterNumber
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.message.buildMessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.limit
import ren.natsuyuk1.setsuna.objects.tweet.ReferencedTweetType
import ren.natsuyuk1.setsuna.objects.tweet.Tweet
import ren.natsuyuk1.setsuna.util.removeShortLink

suspend fun Tweet.toMessageWrapper(): MessageWrapper =
    buildMessageWrapper {
        val tweet = this@toMessageWrapper
        val author = tweet.authorID?.let { TwitterAPI.fetchUser(it) }
        appendText("${author?.name} 发布了一条推文", true)
        appendText(text.removeShortLink().limit(100), true)
        appendLine()
        if (tweet.referencedTweets != null) {
            val rtInfo = tweet.referencedTweets?.firstOrNull()
            if (rtInfo != null) {
                val rt = TwitterAPI.fetchTweet(rtInfo.id)
                val rtAuthor = rt?.authorID?.let { TwitterAPI.fetchUser(it) }

                println(rtAuthor)

                if (rt != null && rtAuthor != null) {
                    when (rtInfo.type) {
                        ReferencedTweetType.RETWEETED -> appendText("\uD83D\uDD01 转发了 ${rtAuthor.name.limit(15)} 的推文", true)
                        ReferencedTweetType.REPLY_TO -> appendText("\uD83D\uDCAC 回复了 ${rtAuthor.name.limit(15)} 的推文", true)
                        ReferencedTweetType.QUOTED -> appendText("\uD83D\uDCAC 引用了 ${rtAuthor.name.limit(15)} 的推文", true)
                        else -> {}
                    }

                    appendText(rt.text.removeShortLink().limit(50), true)
                    appendLine()
                }
            }
        }

        if (tweet.publicMetrics != null) {
            val metrics = tweet.publicMetrics!!
            appendText("💬 ${metrics.reply.getBetterNumber()} \uD83D\uDD01 ${metrics.retweet.getBetterNumber()} 👍 ${metrics.like.getBetterNumber()}")
        }
    }
