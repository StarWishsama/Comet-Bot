package io.github.starwishsama.comet.service.pusher.context

import com.google.gson.annotations.SerializedName
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.live.LiveRoomInfo
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class BiliBiliLiveContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    @SerializedName("custom_status")
    override var status: PushStatus = PushStatus.READY,
    val pushUser: BiliBiliUser,
    var liveRoomInfo: LiveRoomInfo,
): PushContext(pushTarget, retrieveTime, status), Pushable {

    override fun toMessageWrapper(): MessageWrapper {
        val data = liveRoomInfo.data

        if (data.getStatus() != LiveRoomInfo.LiveRoomInfoData.Status.Streaming) {
            return MessageWrapper("未在直播", success = false)
        }

        return MessageWrapper(
            "单推助手 > ${pushUser.userName} 正在直播!" +
                    "\n直播间标题: ${data.title}" +
                    "\n开播时间: ${data.liveTime}" +
                    "\n传送门: ${data.getRoomURL()}",
            success = true
        ).plusImageUrl(data.keyFrameImageUrl)
    }

    override fun contentEquals(other: PushContext): Boolean {
        if (other !is BiliBiliLiveContext) return false

        return liveRoomInfo.data.liveTime == other.liveRoomInfo.data.liveTime
                && liveRoomInfo.data.getStatus() == other.liveRoomInfo.data.getStatus()
    }
}

fun Collection<PushContext>.getLiveContext(uid: Long): BiliBiliLiveContext? {
    val result = this.parallelStream().filter { it is BiliBiliLiveContext && uid == it.pushUser.id.toLong() }.findFirst()
    return if (result.isPresent) result.get() as BiliBiliLiveContext else null
}