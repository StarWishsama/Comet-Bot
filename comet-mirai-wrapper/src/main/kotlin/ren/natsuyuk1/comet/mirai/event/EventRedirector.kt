/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.mirai.event

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import ren.natsuyuk1.comet.api.event.EventManager
import ren.natsuyuk1.comet.mirai.MiraiComet

suspend fun Event.redirectToComet(comet: MiraiComet) {
    when (this) {
        is GroupMessageEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }
    }
}
