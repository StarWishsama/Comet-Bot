package io.github.starwishsama.nbot.objects.twitter

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class TwitterUser(
    // Twitter 数字 ID
    val id: Long,
    @SerializedName("id_str")
    // Twitter 数字 ID (类型为字符串)
    val idString: String,
    // 用户显示昵称
    val name: String,
    // @时候的名字 我也不知道叫什么好
    @SerializedName("screen_name")
    val twitterId: String,
    // 账号上设定的位置
    val location: String?,
    // 个人主页上显示的位置
    @SerializedName("profile_location")
    val profileLocation: String?,
    // 签名
    @SerializedName("description")
    val desc: String?,
    // 个人主页链接
    val url: String?,
    val entities: JsonObject?,
    // 账号是否带锁
    val protected: Boolean,
    // 关注人数
    @SerializedName("followers_count")
    val followersCount: Long,
    @SerializedName("listed_count")
    val listedCount: Long,
    // 账号创建时间
    @SerializedName("created_at")
    val createdTime: String
) {
    override fun toString(): String {
        return "TwitterUser: {id=$id, name=$name, twitterId=$twitterId, desc=$desc}"
    }
}