package ren.natsuyuk1.comet.event.pusher.pushtemplate

import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.event.BroadcastTarget
import ren.natsuyuk1.comet.event.CometBroadcastEvent
import ren.natsuyuk1.comet.objects.config.PushTemplate
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.json.JsonPathMap
import ren.natsuyuk1.comet.utils.json.formatByTemplate

data class PushTemplateReceiveEvent(
    val payload: JsonPathMap,
    val pushTemplate: PushTemplate,
) : CometBroadcastEvent() {
    val content: MessageWrapper = payload.formatByTemplate(pushTemplate.template).toMessageWrapper()

    init {
        pushTemplate.subscribers.forEach {
            broadcastTargets.add(BroadcastTarget(BroadcastTarget.BroadcastType.GROUP, it.id))
        }
    }
}
