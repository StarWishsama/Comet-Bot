package ren.natsuyuk1.comet.api.message

open class MessageSource(
    val from: Long,
    val target: Long,
    val time: Long,
    val messageID: Long
) {
    override fun toString(): String = "MessageSource[from=$from, target=$target, time=$time, messageID=$messageID]"
}
