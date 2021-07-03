/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.api.thirdparty.twitter.data

import com.fasterxml.jackson.annotation.JsonProperty

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