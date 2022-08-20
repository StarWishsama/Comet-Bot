package ren.natsuyuk1.comet.event.pusher.github

import ren.natsuyuk1.comet.event.BroadcastTarget
import ren.natsuyuk1.comet.event.CometBroadcastEvent
import ren.natsuyuk1.comet.objects.github.data.GithubRepoData
import ren.natsuyuk1.comet.objects.github.events.GithubEventData

class GithubEvent(
    private val repo: GithubRepoData.Data.GithubRepo,
    val eventData: GithubEventData
) : CometBroadcastEvent() {
    init {
        val eventType = eventData.type()

        repo.subscribers.forEach {
            if (it.subscribeEvent.contains(eventType)) {
                if (eventData.branchName().isEmpty() || (
                    it.subscribeBranch.isEmpty() || it.subscribeBranch.any { br ->
                        eventData.branchName().matches(Regex(br))
                    }
                    )
                ) {
                    broadcastTargets.add(BroadcastTarget(BroadcastTarget.BroadcastType.GROUP, it.id))
                }
            }
        }
    }

    override fun toString(): String {
        return "Repo=$repo, eventData={type=${eventData.type()}, branch=${eventData.branchName()}}"
    }
}
