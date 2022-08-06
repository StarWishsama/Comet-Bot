package ren.natsuyuk1.comet.event.pusher.github

import ren.natsuyuk1.comet.event.BroadcastTarget
import ren.natsuyuk1.comet.event.CometBroadcastEvent
import ren.natsuyuk1.comet.objects.github.data.GithubRepoData
import ren.natsuyuk1.comet.objects.github.events.GithubEventData

class GithubEvent(
    repo: GithubRepoData.Data.GithubRepo,
    val eventData: GithubEventData
) : CometBroadcastEvent() {
    init {
        val eventType = eventData.type()

        repo.subscribers.forEach {
            if (it.subscribeEvent.contains(eventType)) {
                if (it.subscribeBranch.isEmpty() || it.subscribeBranch.any { br ->
                        eventData.branchName().matches(Regex(br))
                    }) {
                    broadcastTargets.add(BroadcastTarget(BroadcastTarget.BroadcastType.GROUP, it.id))
                }
            }
        }
    }
}
