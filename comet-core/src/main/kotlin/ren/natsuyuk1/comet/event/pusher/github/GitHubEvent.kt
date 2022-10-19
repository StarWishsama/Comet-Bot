package ren.natsuyuk1.comet.event.pusher.github

import ren.natsuyuk1.comet.event.BroadcastTarget
import ren.natsuyuk1.comet.event.CometBroadcastEvent
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.objects.github.events.GitHubEventData

class GitHubEvent(
    private val repo: GitHubRepoData.Data.GithubRepo,
    val eventData: GitHubEventData
) : CometBroadcastEvent() {
    init {
        val eventType = eventData.type()
        val branchName = eventData.branchName()

        repo.subscribers.forEach { sub ->
            if (
                sub.subscribeEvent.contains(eventType) && (
                    sub.subscribeBranch.isEmpty() ||
                        branchName.isBlank() ||
                        sub.subscribeBranch.contains(branchName)
                    )
            ) {
                broadcastTargets.add(
                    BroadcastTarget(
                        BroadcastTarget.BroadcastType.GROUP,
                        sub.id
                    )
                )
            }
        }
    }

    override fun toString(): String {
        return "Repo=$repo, eventData={type=${eventData.type()}, branch=${eventData.branchName()}}"
    }
}
