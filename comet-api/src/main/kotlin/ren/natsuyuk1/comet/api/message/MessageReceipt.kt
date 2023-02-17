package ren.natsuyuk1.comet.api.message

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.Comet
import kotlin.time.Duration

class MessageReceipt(
    /**
     * 参与此消息的 [Comet] 实例
     */
    val comet: Comet,

    val source: MessageSource,
) {
    suspend fun delete() {
        comet.deleteMessage(source)
    }

    fun delayDelete(delay: Duration) = comet.scope.launch {
        delay(delay)
        delete()
    }

    suspend fun reply(message: MessageWrapper): MessageReceipt? = comet.reply(message, this)
}
