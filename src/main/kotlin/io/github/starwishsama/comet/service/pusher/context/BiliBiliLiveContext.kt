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
            return MessageWrapper().addText("未在直播").setUsable(false)
        }

        return MessageWrapper().addText(
            "单推助手 > ${pushUser.userName} 正在直播!" +
                    "\n直播间标题: ${data.title}" +
                    "\n开播时间: ${data.liveTime}" +
                    "\n传送门: ${data.getRoomURL()}",
        ).addPictureByURL(data.keyFrameImageUrl)
    }

    override fun contentEquals(other: PushContext): Boolean {
        if (other !is BiliBiliLiveContext) return false

        return liveRoomInfo.data.getStatus() == other.liveRoomInfo.data.getStatus() && (!liveRoomInfo.data.isEmptyTime() && liveRoomInfo.data.getLiveTime() == other.liveRoomInfo.data.getLiveTime())
    }
}

fun Collection<BiliBiliLiveContext>.getLiveContext(uid: Long): BiliBiliLiveContext? {
    for (blc in this) {
        if (blc.pushUser.id.toLongOrNull() == uid) {
            return blc
        }
    }

    return null
}