package ren.natsuyuk1.comet.api.message

open class MessageSource(
    val from: Long,
    val target: Long,
    val time: Long,
    val messageID: Long
)
