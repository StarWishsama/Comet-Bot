package io.github.starwishsama.comet.objects.pojo.twitter

import cn.hutool.http.HttpException
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.twitter.TwitterApi
import io.github.starwishsama.comet.objects.pojo.twitter.tweetEntity.Media
import io.github.starwishsama.comet.utils.NumberUtil.getBetterNumber
import io.github.starwishsama.comet.utils.toFriendly
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
    @ExperimentalTime
    fun getFullText(): String {
        val duration =
            Duration.between(getSentTime(), LocalDateTime.now())
        val extraText =
            "\n❤${likeCount?.getBetterNumber()}|\uD83D\uDD01${retweetCount}\n\n距离发送已过去了 ${duration.toKotlinDuration()
                .toFriendly(TimeUnit.DAYS)}"

        if (retweetStatus != null) {
            return "转发了 ${retweetStatus.user.name} 的推文\n${retweetStatus.text}"
        }

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
        return text == tweet.text || getSentTime().isEqual(tweet.getSentTime())
    }

    fun getSentTime(): LocalDateTime {
        val twitterTimeFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH)
        return twitterTimeFormat.parse(postTime).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    suspend fun getPictureUrl(): String? {
        val objects = entities

        if (objects != null) {
            val media = objects["media"]
            if (media != null) {
                try {
                    val image =
                        gson.fromJson(objects["media"].asJsonArray[0].asJsonObject.toString(), Media::class.java)
                    if (image.isSendableMedia()) {
                        return image.getImageUrl()
                    }
                } catch (e: JsonSyntaxException) {
                    BotVariables.logger.warning("在获取推文下的图片链接时发生了问题", e)
                } catch (e: HttpException) {
                    BotVariables.logger.warning("在下载推文图片时发生了问题", e)
                }
            }
        }

        if (retweetStatus != null) {
            return retweetStatus.getPictureUrl()
        }

        if (quotedStatus != null) {
            return quotedStatus.getPictureUrl()
        }

        return null
    }
}