package ren.natsuyuk1.comet.event.pusher.github

import ren.natsuyuk1.comet.event.BroadcastTarget
import ren.natsuyuk1.comet.event.CometBroadcastEvent
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData
import ren.natsuyuk1.comet.objects.github.events.GitHubEventData

class GitHubEvent(
    private val repo: GitHubRepoData.Data.GithubRepo,
    val eventData: GitHubEventData
) : CometBroadcastEvent() {
    fun init() {
        val eventType = eventData.type()
        val branchName = eventData.branchName()

        repo.subscribers.forEach { sub ->
            // Check event
            if (!sub.subscribeEvent.contains("*") && !sub.subscribeEvent.contains(eventType)) {
                return@forEach
            }

            // Check branch
            if (!sub.subscribeBranch.contains("*") && !sub.subscribeBranch.contains(branchName)) {
                return@forEach
            }

            broadcastTargets.add(
                BroadcastTarget(
                    BroadcastTarget.BroadcastType.GROUP, sub.id
                )
            )
        }
    }

    override fun toString(): String {
        return "Repo=$repo, eventData={type=${eventData.type()}, branch=${eventData.branchName()}}, broadcastTargets=$broadcastTargets" // ktlint-disable
    }
}
