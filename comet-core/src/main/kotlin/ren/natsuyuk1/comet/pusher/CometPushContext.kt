package ren.natsuyuk1.comet.pusher

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ren.natsuyuk1.comet.api.message.MessageWrapper

abstract class CometPushContext(
    val id: String,
    val target: List<CometPushTarget>,
    val createTime: Instant = Clock.System.now()
) {
    abstract fun normalize(): MessageWrapper

    internal fun toJson(): String = Json.encodeToString(MinCometPushContext(id, target))
}

@Serializable
class MinCometPushContext(
    val id: String,
    val target: List<CometPushTarget>,
    val createTime: Instant = Clock.System.now()
)
