package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.types.enum
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.service.PictureSearchService
import ren.natsuyuk1.comet.objects.command.picturesearch.PictureSearchConfigTable
import ren.natsuyuk1.comet.util.toMessageWrapper

val PICTURESEARCH = CommandProperty(
    "picturesearch",
    listOf("ps", "ytst", "以图搜图"),
    "以图搜图",
    "/ps 以图搜图" +
        "\n\n/ps source [搜索引擎]",
    executeConsumePoint = 10
)

class PictureSearchCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    message: MessageWrapper,
    val user: CometUser
) : CometCommand(comet, sender, subject, message, user, PICTURESEARCH) {
    init {
        subcommands(Source(subject, sender, user))
    }

    override suspend fun run() {
        if (currentContext.invokedSubcommand == null) {
            PictureSearchService.handleSearch(subject, user)
        }
    }

    class Source(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ): CometSubCommand(subject, sender, user, SOURCE) {
        companion object {
            val SOURCE = SubCommandProperty(
                "source",
                listOf("搜索引擎", "ly", "sc"),
                PICTURESEARCH
            )
        }

        val api by argument("API").enum<PictureSearchSource>(ignoreCase = true)

        override suspend fun run() {
            PictureSearchConfigTable.setPlatform(user.id.value, api)
            subject.sendMessage("成功设置以图搜图引擎 ${api.name}".toMessageWrapper())
        }
    }
}

enum class PictureSearchSource {
    SAUCENAO,
    ASCII2D,
}
