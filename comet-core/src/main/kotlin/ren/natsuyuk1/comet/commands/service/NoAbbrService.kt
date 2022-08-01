package ren.natsuyuk1.comet.commands.service

import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.network.thirdparty.nbnhhsh.NoAbbrApi
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper

object NoAbbrService {
    suspend fun PlatformCommandSender.processAbbrSearch(keyword: String) =
        sendMessage(
            NoAbbrApi.search(keyword).firstOrNull()?.toMessageWrapper()
                ?: "找不到 $name 的缩写释义捏".toMessageWrapper()
        )
}
