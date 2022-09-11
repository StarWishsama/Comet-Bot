package ren.natsuyuk1.comet.api.message

open class MessageSource(
    val type: MessageSourceType,
    val from: Long,
    val target: Long,
    val time: Long,
    val messageID: Long
) {
    enum class MessageSourceType {
        GROUP,
        FRIEND,
        TEMP,
        STRANGER,
        /* For Telegram */
        BOT
    }

    override fun toString(): String = "MessageSource[from=$from, target=$target, time=$time, messageID=$messageID]"
}
