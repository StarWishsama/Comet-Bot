package ren.natsuyuk1.comet.event.pusher.github

import ren.natsuyuk1.comet.event.CometBroadcastEvent
import ren.natsuyuk1.comet.utils.message.MessageWrapper

class GithubWebHookEvent(
    val repo: String,
    val owner: String,
    val context: MessageWrapper
) : CometBroadcastEvent()
