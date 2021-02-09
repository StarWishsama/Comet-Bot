package io.github.starwishsama.comet.api.thirdparty.twitter.data.tweetEntity

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.BotVariables

/**
 * 推文中的媒体实体类
 */
data class Media(
    /**
     * 对外展示的链接
     */
    @SerializedName("display_url")
    val displayUrl: String,
    /**
     * 对外展示链接的扩展版本
     */
    @SerializedName("expanded_url")
    val expandedUrl: String,
    /**
     * Int64 的媒体实体 ID.
     */
    val id: Long,
    /**
     * 媒体实体 ID 的字符串版本
     */
    @SerializedName("id_str")
    val idAsString: String,
    /**
     * An array of integers indicating the offsets within the Tweet text where the URL begins and ends.
     * The first integer represents the location of the first character of the URL in the Tweet text.
     * The second integer represents the location of the first non-URL character occurring after the URL
     * (or the end of the string if the URL is the last part of the Tweet text).
     */
    val indices: List<Int>,
    /**
     * 媒体文件链接 (HTTP)
     */
    @SerializedName("media_url")
    val mediaUrlHttp: String,
    /**
     * 媒体文件链接 (HTTPS)
     */
    @SerializedName("media_url_https")
    val mediaUrlHttps: String,
    /**
     * 媒体实体类型
     * 可选的类型有: [photo, video, animated_gif]
     */
    val type: String
) {
    fun isSendableMedia(): Boolean {
        return !type.contentEquals("video")
    }

    fun getImageUrl(): String {
        val imgType = arrayOf("jpg", "png", "jpeg", "webp", "heif", "gif")
        if (BotVariables.cfg.smallImageMode) {
            var type = ""
            imgType.forEach {
                if (mediaUrlHttps.contains(it)) {
                    type = it
                }
            }

            if (type.isBlank()) return mediaUrlHttps

            return mediaUrlHttps.replace(".$type".toRegex(), "") + "?format=$type&name=small"
        }
        return mediaUrlHttps
    }
}