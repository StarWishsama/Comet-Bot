package ren.natsuyuk1.comet.objects.github.events

import ren.natsuyuk1.comet.event.BroadcastTarget
import ren.natsuyuk1.comet.event.CometBroadcastEvent
import ren.natsuyuk1.comet.objects.github.data.GithubRepoData

class GithubEvent(
    repo: GithubRepoData.Data.GithubRepo,
    eventData: GithubEventData
) : CometBroadcastEvent() {
    init {
        val eventType = eventData.type()

        repo.subscribers.forEach {
            if (it.subscribeEvent.contains(eventType)) {
                broadcastTargets.add(BroadcastTarget(BroadcastTarget.BroadcastType.GROUP, it.id))
            }
        }
    }
}
