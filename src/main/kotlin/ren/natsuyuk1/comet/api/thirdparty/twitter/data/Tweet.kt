/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.api.thirdparty.twitter.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.hmsPattern
import io.github.starwishsama.comet.CometVariables.mapper
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.api.thirdparty.twitter.data.tweetEntity.Media
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.NumberUtil.getBetterNumber
import io.github.starwishsama.comet.utils.StringUtil.toFriendly
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
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
    @JsonProperty("created_at")
    val postTime: String,
    val id: Long,
    @JsonProperty("id_str")
    val idAsString: String,
    @JsonProperty("full_text")
    val text: String,
    val truncated: Boolean,
    val entities: JsonNode?,
    val source: String,
    @JsonProperty("in_reply_to_status_id")
    val replyTweetId: Long?,
    val user: TwitterUser,
    @JsonProperty("retweeted_status")
    val retweetStatus: Tweet?,
    @JsonProperty("retweet_count")
    val retweetCount: Long?,
    @JsonProperty("favorite_count")
    val likeCount: Long?,
    @JsonProperty("possibly_sensitive")
    val sensitive: Boolean?,
    @JsonProperty("quoted_status")
    val quotedStatus: Tweet?,
    @JsonProperty("is_quote_status")
    val isQuoted: Boolean
) {
    /**
     * 格式化输出推文
     */
    @OptIn(ExperimentalTime::class)
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

        /**
         * 从此推文中获取图片链接
         */

        val media = entities?.get("media")
        if (media != null) {
            try {
                val image = mapper.readValue(media[0].traverse(), Media::class.java)
                if (image.isSendableMedia()) {
                    return image.getImageUrl()
                }
            } catch (e: RuntimeException) {
                CometVariables.logger.warning("在获取推文下的图片链接时发生了问题", e)
            }
        }

        // 避免套娃
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

    fun toMessageWrapper(): MessageWrapper {
        return MessageWrapper().addText(convertToString()).apply {
            val url = getPictureUrl()
            if (url != null) {
                addPictureByURL(url)
            }
        }
    }

    fun toMessageChain(target: Contact): MessageChain {
        return runBlocking { toMessageWrapper().toMessageChain(target) }
    }

    private fun getTweetURL(): String {
        return "https://twitter.com/${user.twitterId}/status/$idAsString"
    }
}