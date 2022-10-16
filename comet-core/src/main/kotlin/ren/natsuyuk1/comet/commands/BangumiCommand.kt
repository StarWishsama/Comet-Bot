package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.network.thirdparty.bangumi.BangumiOnlineApi
import ren.natsuyuk1.comet.network.thirdparty.bangumi.bgmCrawler
import ren.natsuyuk1.comet.network.thirdparty.bangumi.data.common.SearchType
import ren.natsuyuk1.comet.network.thirdparty.bangumi.parser.toMessageWrapper
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.string.parseDayOfWeek

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
        if (currentContext.invokedSubcommand == null) {
            subject.sendMessage(property.helpText.toMessageWrapper())
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
            val SCHEDULE = SubCommandProperty("schedule", listOf("搜索", "sch", "calendar", "cal"), BANGUMI)
        }

        private val dayOfWeek by argument("星期几").default("")

        override suspend fun run() {
            if (dayOfWeek.isBlank()) {
                subject.sendMessage(BangumiOnlineApi.getCache().getSpecificDaySchedule())
            } else {
                val dow = parseDayOfWeek(dayOfWeek)

                if (dow == null) {
                    subject.sendMessage("请输入有效的星期名称!".toMessageWrapper())
                } else {
                    subject.sendMessage(BangumiOnlineApi.getCache().getSpecificDaySchedule(dow))
                }
            }
        }
    }
}
