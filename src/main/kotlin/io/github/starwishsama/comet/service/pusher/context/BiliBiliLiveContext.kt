/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher.context

import io.github.starwishsama.comet.api.thirdparty.bilibili.live.getStatus
import io.github.starwishsama.comet.api.thirdparty.bilibili.live.isLiveTimeInvalid
import io.github.starwishsama.comet.api.thirdparty.bilibili.live.parseLiveTime
import io.github.starwishsama.comet.objects.push.BiliBiliUser
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.service.pusher.PushStatus
import moe.sdl.yabapi.data.live.LiveRoomData

class BiliBiliLiveContext(
    pushTarget: MutableSet<Long>,
    retrieveTime: Long,
    status: PushStatus = PushStatus.PENDING,
    val pushUser: BiliBiliUser,
    val liveRoomInfo: LiveRoomData,
) : PushContext(pushTarget, retrieveTime, status), Pushable {
    override fun toMessageWrapper(): MessageWrapper {
        if (liveRoomInfo.liveStatus != 1) {
            return MessageWrapper().addText("未在直播").setUsable(false)
        }

        return MessageWrapper().addText(
            "直播间助手 > ${pushUser.userName} 正在直播!" +
                    "\n直播间标题: ${liveRoomInfo.title}" +
                    "\n开播时间: ${liveRoomInfo.liveTime}" +
                    "\n传送门: https://live.bilibili.com/${liveRoomInfo.roomId}",
        ).addPictureByURL(liveRoomInfo.userCover)
    }

    override fun contentEquals(other: PushContext): Boolean {
        if (other !is BiliBiliLiveContext) return false

        return liveRoomInfo.getStatus() == other.liveRoomInfo.getStatus()
                && (!liveRoomInfo.isLiveTimeInvalid() && liveRoomInfo.parseLiveTime() == other.liveRoomInfo.parseLiveTime())
    }
}