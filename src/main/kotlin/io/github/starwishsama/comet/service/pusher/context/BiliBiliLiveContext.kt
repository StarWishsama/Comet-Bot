package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.api.thirdparty.bilibili.data.live.LiveRoomInfo
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

class BiliBiliLiveContext(
    pushTarget: MutableList<Long>,
    retrieveTime: Long,
    override var status: PushStatus = PushStatus.READY,
    val pushUser: BiliBiliUser,
    var liveStatus: LiveRoomInfo,
): PushContext(pushTarget, retrieveTime, status) {

    override fun toMessageWrapper(): MessageWrapper {
        val data = liveStatus.data

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

    override fun compareTo(other: PushContext): Boolean {
        if (other !is BiliBiliLiveContext) return false

        return liveStatus.data.liveTime == other.liveStatus.data.liveTime
    }
}

fun Collection<BiliBiliLiveContext>.getContextByUID(uid: Long): BiliBiliLiveContext? {
    val result = this.parallelStream().filter { uid == it.pushUser.id.toLong() }.findFirst()
    return if (result.isPresent) result.get() else null
}