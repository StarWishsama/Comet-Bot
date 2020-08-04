package io.github.starwishsama.comet.objects.pojo.twitter

import cn.hutool.http.HttpException
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.objects.pojo.twitter.tweetEntity.Media
import io.github.starwishsama.comet.utils.NetUtil
import io.github.starwishsama.comet.utils.toFriendly
import io.github.starwishsama.comet.utils.toMsgChain
import io.github.starwishsama.comet.utils.uploadAsImageSafely
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

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
        val retweetStatus: ReTweet?,
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
    @ExperimentalTime
    fun getFullText(): String {
        val duration =
                Duration.between(getSentTime(), LocalDateTime.now())
        val extraText = "\n❤${likeCount}|\uD83D\uDD01${retweetCount}\n\n距离发送已过去了 ${duration.toKotlinDuration().toFriendly(TimeUnit.DAYS)}"

        if (isQuoted && quotedStatus != null) {
            return "对于 ${quotedStatus.user.name} 的推文\n${quotedStatus.text}\n\n${user.name} 进行了评论\n$text" + extraText
        }

        if (replyTweetId != null) {
            val repliedTweet = TwitterApi.getTweetById(replyTweetId) ?: return text + extraText
            return "对于 ${repliedTweet.user.name} 的推文\n${repliedTweet.text}\n\n${user.name} 进行了回复\n$text" + extraText
        }

        return text + extraText
    }

    fun contentEquals(tweet: Tweet): Boolean {
        return text.contentEquals(tweet.text) || getSentTime().isEqual(tweet.getSentTime())
    }

    fun getSentTime(): LocalDateTime {
        val twitterTimeFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH)
        return twitterTimeFormat.parse(postTime).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    suspend fun getPictureOrNull(contact: Contact): Image? {
        val objects = entities
        var picture: Image? = null

        if (objects != null) {
            val media = objects["media"]
            if (media != null) {
                try {
                    val image = gson.fromJson(objects["media"].asJsonArray[0].asJsonObject.toString(), Media::class.java)
                    if (image.isSendableMedia()) {
                        val response = NetUtil.doHttpRequestGet(image.getImageUrl(), 8000).executeAsync()
                        if (response.isOk) {
                            picture = NetUtil.getUrlInputStream(image.getImageUrl()).uploadAsImageSafely(response.header("content-type"), contact)
                        }
                    }
                } catch (e: JsonSyntaxException) {
                    BotVariables.logger.warning("在获取推文下的图片链接时发生了问题", e)
                } catch (e: HttpException) {
                    BotVariables.logger.warning("在下载推文图片时发生了问题", e)
                }
            }
        }

        if (retweetStatus != null) {
            val image = retweetStatus.getPictureOrNull(contact)
            picture = image
        }

        if (quotedStatus != null && picture == null) {
            picture = quotedStatus.getPictureOrNull(contact)
        }

        return picture
    }

    @ExperimentalTime
    suspend fun getAsMessageChain(contact: Contact?): MessageChain {
        if (contact == null || getPictureOrNull(contact) == null) return getFullText().toMsgChain()

        val image = getPictureOrNull(contact)
        return getFullText().toMsgChain() + (image ?: EmptyMessageChain)
    }
}