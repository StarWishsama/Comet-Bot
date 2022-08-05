package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.network.thirdparty.bangumi.BangumiOnlineApi
import ren.natsuyuk1.comet.network.thirdparty.bangumi.bgmCrawler
import ren.natsuyuk1.comet.network.thirdparty.bangumi.data.common.SearchType
import ren.natsuyuk1.comet.network.thirdparty.bangumi.parser.toMessageWrapper
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toArgs
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper

val BANGUMI = CommandProperty(
    "bangumi",
    listOf("bgm", "番剧"),
    "查询番剧信息",
    """
    /bgm search 搜索番剧信息
    /bgm schedule 查询今日番剧放送表    
    """.trimIndent()
)

class BangumiCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser
) : CometCommand(comet, sender, subject, message, user, BANGUMI) {
    init {
        subcommands(
            Search(subject, sender, user),
            Schedule(subject, sender, user)
        )
    }

    override suspend fun run() {
        if (message.parseToString().toArgs().size == 1) {
            subject.sendMessage(BANGUMI.helpText.toMessageWrapper())
        }
    }

    class Search(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, SEARCH) {
        companion object {
            val SEARCH = SubCommandProperty("search", listOf("搜索"), BANGUMI)
        }

        private val keyword by argument("搜索词")

        override suspend fun run() {
            val searchSubject = bgmCrawler.searchSubject(SearchType.Subject.All, keyword).items.firstOrNull()

            if (searchSubject == null) {
                subject.sendMessage("搜索不到与关键词有关的条目".toMessageWrapper())
                return
            }

            bgmCrawler.fetchSubject(searchSubject.url).run {
                subject.sendMessage(toMessageWrapper(searchSubject.url))
            }
        }
    }

    class Schedule(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, SCHEDULE) {
        companion object {
            val SCHEDULE = SubCommandProperty("schedule", listOf("搜索"), BANGUMI)
        }

        override suspend fun run() {
            subject.sendMessage(BangumiOnlineApi.getCache().getSpecificDaySchedule())
        }
    }
}
