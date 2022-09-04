package ren.natsuyuk1.comet.pusher

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment
import java.util.concurrent.TimeUnit

@Serializable
data class CometPusherConfig(
    @Comment("推送器的推送周期, 单位见 pushIntervalUnit.")
    val pushInterval: Int,
    @Comment("夜间模式推送周期, 默认为一小时, 单位见 pushIntervalUnit")
    val nightPushInterval: Int = 3600,
    @Comment("推送器夜间模式时期, 格式为 [开始时]-[结束时], 例如 23-6 指今日 23 点到次日 6 点为夜间模式时期.")
    val nightModeDuration: String = "0-6",
    @Comment("推送器的推送周期时间单位, 默认为秒.")
    val pushIntervalUnit: TimeUnit = TimeUnit.SECONDS,
)
