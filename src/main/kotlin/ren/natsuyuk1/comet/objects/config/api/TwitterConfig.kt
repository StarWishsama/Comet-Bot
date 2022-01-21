/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.objects.config.api

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import java.util.concurrent.TimeUnit

@Serializable
data class TwitterConfig(
    @Comment("用于获取 Twitter Token 的 Access Token, 使用 Twitter 推送必填")
    var accessToken: String = "",
    @Comment("用于获取 Twitter Token 的 Access Secret, 使用 Twitter 推送必填")
    val accessSecret: String = "",
    @Comment("Twitter 开发者 API 访问 Token, 上述填写后会自动获取, 无需填写")
    var token: String = "",
    @Comment("推文推送时候是否发送小图而不是原图")
    var smallImageMode: Boolean = true,
    @Comment("查询 Twitter 用户动态间隔时间")
    override val interval: Int = 5,
    @Comment("查询 Twitter 用户动态间隔时间单位, 默认为分钟")
    override val timeUnit: TimeUnit = TimeUnit.MINUTES
) : ApiConfig {
    override val apiName: String = "twitter"
}