package io.github.starwishsama.nbot.objects.pojo.twitter

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

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
)