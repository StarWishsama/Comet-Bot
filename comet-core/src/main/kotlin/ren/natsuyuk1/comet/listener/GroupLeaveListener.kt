package ren.natsuyuk1.comet.listener

import ren.natsuyuk1.comet.api.event.events.group.GroupLeaveEvent
import ren.natsuyuk1.comet.api.group.GroupSettingManager
import ren.natsuyuk1.comet.api.listener.CometListener
import ren.natsuyuk1.comet.api.listener.EventHandler
import ren.natsuyuk1.comet.objects.github.data.GitHubRepoData

object GroupLeaveListener : CometListener {
    override val name: String
        get() = "群聊变动"

    @EventHandler
    fun groupLeave(event: GroupLeaveEvent) {
        if (event.comet.id == event.user.id) {
            GitHubRepoData.data.repos.forEach {
                it.subscribers.removeIf { group ->
                    group.id == event.group.id
                }
            }

            GroupSettingManager.removeGroupConfig(event.group.id, event.group.platform)
        }
    }
}
