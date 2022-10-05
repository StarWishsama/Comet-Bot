package ren.natsuyuk1.comet.mirai

import ren.natsuyuk1.comet.api.message.MessageSource

/**
 * [MiraiMessageSource]
 *
 * **Mirai 侧独占** 消息元素.
 */
class MiraiMessageSource(
    val miraiSource: net.mamoe.mirai.message.data.MessageSource,
    type: MessageSourceType,
    time: Int,
    from: Long,
    target: Long,
) : MessageSource(type, from, target, time.toLong(), (miraiSource.ids.firstOrNull() ?: -1).toLong())
