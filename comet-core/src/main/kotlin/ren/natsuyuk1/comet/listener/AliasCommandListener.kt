package ren.natsuyuk1.comet.listener

import ren.natsuyuk1.comet.api.event.events.message.MessageEvent
import ren.natsuyuk1.comet.api.listener.CometListener
import ren.natsuyuk1.comet.api.listener.EventHandler
import ren.natsuyuk1.comet.objects.config.AliasCommandHandler

object AliasCommandListener : CometListener {
    override val name: String
        get() = "命令别名"

    @EventHandler
    suspend fun processKeyword(event: MessageEvent) {
        AliasCommandHandler.handle(
            event.comet,
            event.sender,
            event.subject,
            event.message,
        )
    }
}
