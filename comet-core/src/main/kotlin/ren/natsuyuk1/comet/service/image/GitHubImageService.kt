package ren.natsuyuk1.comet.service.image

import ren.natsuyuk1.comet.objects.github.events.GithubEventData
import ren.natsuyuk1.comet.objects.github.events.PullRequestEventData
import java.io.File

object GitHubImageService {
    fun drawEventInfo(event: GithubEventData): File {
        return when (event) {
            is PullRequestEventData -> {
                TODO()
            }

            else -> error("不支持转换的事件, 请使用文本转换.")
        }
    }

    private fun PullRequestEventData.drawPullRequestEvent(): File {
        TODO()
    }
}
