package io.github.starwishsama.comet.api.thirdparty.twitter.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class TwitterUser(
    // Twitter 数字 ID
    val id: Long,
    @JsonProperty("id_str")
    // Twitter 数字 ID (类型为字符串)
    val idString: String,
    // 用户显示昵称
    val name: String,
    // @时候的名字 我也不知道叫什么好
    @JsonProperty("screen_name")
    val twitterId: String,
    // 账号上设定的位置
    val location: String?,
    // 个人主页上显示的位置
    @JsonProperty("profile_location")
    val profileLocation: String?,
    // 签名
    @JsonProperty("description")
    val desc: String?,
    // 个人主页链接
    val url: String?,
    val entities: JsonNode?,
    // 账号是否带锁
    val protected: Boolean,
    // 关注人数
    @JsonProperty("followers_count")
    val followersCount: Long,
    @JsonProperty("listed_count")
    val listedCount: Long,
    // 账号创建时间
    @JsonProperty("created_at")
    val createdTime: String
) {
    override fun toString(): String {
        return "TwitterUser: {id=$id, name=$name, twitterId=$twitterId, desc=$desc}"
    }
}