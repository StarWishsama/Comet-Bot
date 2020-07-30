package io.github.starwishsama.comet.objects.pojo.twitter

import cn.hutool.http.HttpException
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.gson
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
        var postTime: String,
        var id: Long,
        @SerializedName("id_str")
        var idString: String,
        @SerializedName("full_text")
        var text: String,
        var truncated: Boolean,
        var entities: JsonObject?,
        var source: String,
        var user: TwitterUser,
        @SerializedName("retweeted_status")
        var retweetStatus: ReTweet?,
        @SerializedName("retweet_count")
        var retweetCount: Long,
        @SerializedName("favorite_count")
        var likeCount: Long,
        @SerializedName("possibly_sensitive")
        var sensitive: Boolean,
        @SerializedName("quoted_status")
        var quotedStatus : Tweet?,
        @SerializedName("is_quote_status")
        var isQuoted: Boolean
) {
    @ExperimentalTime
    fun getFullText(): String {
        val quoted = quotedStatus
        var result = text

        if (isQuoted && quoted != null) {
            result = "对推文进行了回复\n$text\n\n引用推文\n${quoted.text}"
        }

        val duration =
            Duration.between(getSentTime(), LocalDateTime.now())
        result += "\n\n距离发送已过去了 ${duration.toKotlinDuration().toFriendly(TimeUnit.DAYS)}"

        return result
    }

    fun contentEquals(tweet: Tweet): Boolean {
        return text == tweet.text
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
            val image = retweetStatus?.getPictureOrNull(contact)
            picture = image
        }

        if (quotedStatus != null && picture == null) {
            picture = quotedStatus?.getPictureOrNull(contact)
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