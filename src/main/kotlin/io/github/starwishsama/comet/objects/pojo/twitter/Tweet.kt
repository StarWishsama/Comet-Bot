package io.github.starwishsama.comet.objects.pojo.twitter

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.BotVariables.hmsPattern
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.objects.pojo.twitter.tweetEntity.Media
import io.github.starwishsama.comet.utils.NumberUtil.getBetterNumber
import io.github.starwishsama.comet.utils.StringUtil.toFriendly
import io.github.starwishsama.comet.utils.network.NetUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.uploadAsImage
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.regex.Pattern
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

val tcoPattern: Pattern = Pattern.compile("https://t.co/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")

data class Tweet(
        @SerializedName("created_at")
        val postTime: String,
        val id: Long,
        @SerializedName("id_str")
        val idAsString: String,
        @SerializedName("full_text")
        val text: String,
        val truncated: Boolean,
        val entities: JsonObject?,
        val source: String,
        @SerializedName("in_reply_to_status_id")
        val replyTweetId: Long?,
        val user: TwitterUser,
        @SerializedName("retweeted_status")
        val retweetStatus: Tweet?,
        @SerializedName("retweet_count")
        val retweetCount: Long?,
        @SerializedName("favorite_count")
        val likeCount: Long?,
        @SerializedName("possibly_sensitive")
        val sensitive: Boolean?,
        @SerializedName("quoted_status")
        val quotedStatus: Tweet?,
        @SerializedName("is_quote_status")
        val isQuoted: Boolean
) {
    /**
     * 格式化输出推文
     */
    @ExperimentalTime
    fun convertToString(): String {
        val duration =
                Duration.between(getSentTime(), LocalDateTime.now())
        val extraText =
            "❤${likeCount?.getBetterNumber()} | \uD83D\uDD01${retweetCount} | 🕘${hmsPattern.format(getSentTime())}"

        if (retweetStatus != null) {
            return "转发了 ${retweetStatus.user.name} 的推文\n" +
                    "${cleanShortUrlAtEnd(retweetStatus.text)}\n" +
                    "$extraText\n" +
                    "\uD83D\uDD17 > ${getTweetURL()}\n" +
                    "在 ${duration.toKotlinDuration().toFriendly(msMode = false)} 前发送"
        }

        if (isQuoted && quotedStatus != null) {
            return "对于 ${quotedStatus.user.name} 的推文\n" +
                    "${cleanShortUrlAtEnd(quotedStatus.text)}\n" +
                    "\n${user.name} 进行了评论\n" +
                    "${cleanShortUrlAtEnd(text)}\n" +
                    "$extraText\n🔗 > ${getTweetURL()}\n" +
                    "在 ${duration.toKotlinDuration().toFriendly(msMode = false)} 前发送"
        }

        if (replyTweetId != null) {
            val repliedTweet = TwitterApi.getTweetById(replyTweetId) ?: return "${cleanShortUrlAtEnd(text)}\n" +
                    "$extraText\n" +
                    "🔗 > ${getTweetURL()}\n" +
                    "在 ${duration.toKotlinDuration().toFriendly(msMode = false)} 前发送"

            return "对于 ${repliedTweet.user.name} 的推文:\n" +
                    "${cleanShortUrlAtEnd(repliedTweet.text)}\n\n" +
                    "${user.name} 进行了回复\n${cleanShortUrlAtEnd(text)}\n" +
                    "$extraText\n🔗 > ${getTweetURL()}\n" +
                    "在 ${duration.toKotlinDuration().toFriendly(msMode = false)} 前发送"
        }

        return "${cleanShortUrlAtEnd(text)}\n" +
                "$extraText\n" +
                "🔗 > ${getTweetURL()}\n" +
                "在 ${duration.toKotlinDuration().toFriendly(msMode = false)} 前发送"
    }

    /**
     * 判断两个推文是否内容相同
     */
    fun contentEquals(tweet: Tweet?): Boolean {
        if (tweet == null) return false
        return id == tweet.id || text == tweet.text || getSentTime().isEqual(tweet.getSentTime())
    }

    /**
     * 获取该推文发送的时间
     */
    fun getSentTime(): LocalDateTime {
        val twitterTimeFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH)
        return twitterTimeFormat.parse(postTime).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    /**
     * 获取推文中的第一张图片
     */
    private fun getPictureUrl(nestedMode: Boolean = false): String? {
        val jsonEntities = entities

        /**
         * 从此推文中获取图片链接
         */

        val media = jsonEntities?.get("media")
        if (media != null) {
            try {
                val image =
                        gson.fromJson(media.asJsonArray[0].asJsonObject.toString(), Media::class.java)
                if (image.isSendableMedia()) {
                    return image.getImageUrl()
                }
            } catch (e: RuntimeException) {
                BotVariables.logger.warning("在获取推文下的图片链接时发生了问题", e)
            }
        }

        // 避免套娃
        // FIXME: 逻辑错误?
        if (!nestedMode) {
            /**
             * 如果推文中没有图片, 则尝试获取转推中的图片
             */
            if (retweetStatus != null) {
                return retweetStatus.getPictureUrl(true)
            }

            /**
             * 如果推文中没有图片, 则尝试获取引用回复推文中的图片
             */
            if (quotedStatus != null) {
                return quotedStatus.getPictureUrl(true)
            }
        }

        return null
    }

    /**
     * 清理推文中末尾的 t.co 短链
     */
    private fun cleanShortUrlAtEnd(tweet: String): String {
        val tcoUrl = mutableListOf<String>()

        tcoPattern.matcher(tweet).run {
            while (find()) {
                tcoUrl.add(group())
            }
        }

        return if (tcoUrl.isNotEmpty()) tweet.replace(tcoUrl.last(), "") else tweet
    }

    @ExperimentalTime
    fun toMessageChain(target: Contact): MessageChain {
        return MessageChainBuilder().apply {
            append(convertToString())
            val url = getPictureUrl(true) ?: return this.asMessageChain()

            val image = runBlocking { NetUtil.getUrlInputStream(url)?.uploadAsImage(target) }

            if (image != null) {
                append(image)
            }
        }.asMessageChain()
    }

    fun getTweetURL(): String {
        return "https://twitter.com/${user.twitterId}/status/$idAsString"
    }
}