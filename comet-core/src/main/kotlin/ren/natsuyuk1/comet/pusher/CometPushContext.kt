package ren.natsuyuk1.comet.pusher

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class CometPushContext(
    val id: String,
    val target: List<CometPushTarget>
){
    abstract fun normalize(): MessageWrapper

    internal fun toJson(): String = json.encodeToString(MinCometPushContext(id, target))
}

@Serializable
class MinCometPushContext(val id: String, val target: List<CometPushTarget>)
