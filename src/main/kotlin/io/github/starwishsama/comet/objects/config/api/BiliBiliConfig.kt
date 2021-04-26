package io.github.starwishsama.comet.objects.config.api

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import java.util.concurrent.TimeUnit

@Serializable
data class BiliBiliConfig(
    @Comment("哔哩哔哩登录账号名, 支持手机号/邮箱")
    val login: String = "",
    @Comment("哔哩哔哩账号密码")
    val password: String = "",
    @Comment("哔哩哔哩登录 Cookie, 使用此项时上面两项可留空.\n不留空尝试登录成功后也会自动保存到这里")
    var cookie: String = "",
    @Comment("动态/直播推送获取周期, 默认单位为分钟")
    override val interval: Int = 2,
    @Comment("获取周期的单位, 可选的有 MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS")
    override val timeUnit: TimeUnit = TimeUnit.MINUTES
) : ApiConfig {
    override val apiName: String = "bilibili"
}