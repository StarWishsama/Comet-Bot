package ren.natsuyuk1.comet.mirai

import ren.natsuyuk1.comet.api.message.MessageSource

/**
 * [MiraiMessageSource]
 *
 * **Mirai 侧独占** 消息元素.
 */
class MiraiMessageSource(
    val kind: MiraiMessageSourceKind,
    val botID: Long,
    val ids: IntArray,
    val internalIds: IntArray,
    time: Int,
    from: Long,
    target: Long,
): MessageSource(from, target, time.toLong(), (ids.firstOrNull() ?: -1).toLong()) {
    enum class MiraiMessageSourceKind {
        GROUP,
        FRIEND,
        TEMP,
        STRANGER
    }
}
