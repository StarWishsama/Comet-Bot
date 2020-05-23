package io.github.starwishsama.nbot.objects.pojo.twitter

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import io.github.starwishsama.nbot.BotConstants.gson
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.objects.pojo.twitter.tweetEntity.Media
import io.github.starwishsama.nbot.util.BotUtil
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.uploadAsImage

data class Tweet(
    @SerializedName("created_at")
    var postTime: String,
    var id: Long,
    @SerializedName("id_str")
    var idString: String,
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
    var sensitive: Boolean
) {
    suspend fun getPictureOrNull(contact: Contact) : Image? {
        val objects = entities
        if (objects != null) {
            val media = objects["media"]
            if (media != null) {
                try {
                    val image = gson.fromJson(objects["media"].asJsonArray[0].asJsonObject.toString(), Media::class.java)
                    if (image.isSendableMedia()) {
                        return BotUtil.getImageStream(image.mediaUrlHttps).uploadAsImage(contact)
                    }
                } catch (e: JsonSyntaxException) {
                    BotMain.logger.error("在获取推文下的图片时发生了问题", e)
                }
            }
        }
        return null
    }
}