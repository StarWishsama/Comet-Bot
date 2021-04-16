package io.github.starwishsama.comet.objects.config.api

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