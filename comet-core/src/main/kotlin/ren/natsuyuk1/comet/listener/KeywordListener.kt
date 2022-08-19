package ren.natsuyuk1.comet.listener

import kotlinx.datetime.Clock
import ren.natsuyuk1.comet.api.command.hasCoolDown
import ren.natsuyuk1.comet.api.event.events.message.GroupMessageEvent
import ren.natsuyuk1.comet.api.listener.CometListener
import ren.natsuyuk1.comet.api.listener.EventHandler
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.objects.keyword.KeyWordData

object KeywordListener : CometListener {
    override val name: String
        get() = "关键字回复"

    @EventHandler
    fun processKeyword(event: GroupMessageEvent) {
        val triggerTime = Clock.System.now()
        val sender = event.sender
        val user = CometUser.getUser(sender.id, sender.platform) ?: return

        if (user.hasCoolDown(triggerTime)) return

        val keywords = KeyWordData.find(event.subject.id, event.subject.platform) ?: return
        val message = event.message.parseToString()

        keywords.words.forEach { kw ->
            val validate = if (kw.isRegex) {
                message.matches(Regex(kw.pattern))
            } else {
                message == kw.pattern
            }

            if (validate) {
                event.subject.sendMessage(kw.reply)
            }
        }
    }
}
