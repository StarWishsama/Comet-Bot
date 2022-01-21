/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.service.pusher.context

import io.github.starwishsama.comet.objects.wrapper.MessageWrapper

/**
 * [Pushable]
 *
 * 声明其为一个可推送的类, 用于 [CometPusher] 推送
 */
interface Pushable {
    /**
     * 转换为通用的 [MessageWrapper]
     */
    fun toMessageWrapper(): MessageWrapper

    /**
     * 检查一个 [PushContext] 是否和自身内容相同
     */
    fun contentEquals(other: PushContext): Boolean
}