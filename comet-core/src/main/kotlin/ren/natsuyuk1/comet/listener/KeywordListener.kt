package ren.natsuyuk1.comet.listener

import ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent
import ren.natsuyuk1.comet.api.listener.CometListener
import ren.natsuyuk1.comet.api.listener.EventHandler

object KeywordListener : CometListener {
    override val name: String
        get() = "关键字回复"

    @EventHandler
    fun processKeyword(event: GroupMessageEvent) {

    }
}
