package ren.natsuyuk1.comet.listener

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ren.natsuyuk1.comet.api.config.CometGlobalConfig
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.listener.CometListener
import ren.natsuyuk1.comet.api.listener.EventHandler
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.task.TaskManager
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
object RateLimitListener : CometListener {
    private val messageCache = Channel<MessagePreSendEvent>(CometGlobalConfig.data.globalRateLimitMessageSize)
    override val name: String
        get() = "全局限速"

    init {
        TaskManager.registerTaskDelayed(CometGlobalConfig.data.globalRateLimitInterval.minutes) {
            var count = 1

            while (count < CometGlobalConfig.data.globalRateLimitMessageSize && !messageCache.isEmpty) {
                messageCache.consume {
                    receive().apply {
                        target.sendMessage(message)
                    }

                    delay((500L..2000L).random())

                    count++
                }
            }
        }
    }

    @EventHandler
    fun processMessage(event: MessagePreSendEvent) {
        if (event.target.platform == LoginPlatform.MIRAI) {
            event.comet.scope.launch {
                messageCache.send(event)
            }
        }
    }
}
