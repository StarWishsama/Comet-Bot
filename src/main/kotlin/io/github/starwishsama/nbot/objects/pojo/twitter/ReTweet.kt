package io.github.starwishsama.nbot.objects.pojo.twitter

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.objects.pojo.twitter.tweetEntity.Media
import io.github.starwishsama.nbot.utils.NetUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage

data class ReTweet (
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
        var retweetStatus: JsonObject?,
        @SerializedName("retweet_count")
        var retweetCount: Long,
        @SerializedName("favorite_count")
        var likeCount: Long,
        @SerializedName("possibly_sensitive")
        var sensitive: Boolean,
        @SerializedName("quoted_status")
        var quotedTweet: Tweet?
) {

    suspend fun getPictureOrNull(contact: Contact) : Image? {
        val objects = entities
        var picture : Image? = null

        if (objects != null) {
            val media = objects["media"]
            if (media != null) {
                try {
                    val image = BotConstants.gson.fromJson(objects["media"].asJsonArray[0].asJsonObject.toString(), Media::class.java)
                    if (image.isSendableMedia()) {
                        picture = NetUtil.getUrlInputStream(image.mediaUrlHttps).uploadAsImage(contact)
                    }
                } catch (e: JsonSyntaxException) {
                    BotMain.logger.error("在获取推文下的图片时发生了问题", e)
                }
            }
        }

        val quotedTweet = quotedTweet
        if (quotedTweet != null) {
            picture = quotedTweet.getPictureOrNull(contact)
        }

        return picture
    }
}