package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.bgm.bgmCrawler
import io.github.starwishsama.comet.api.thirdparty.bgm.data.common.SearchType
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import io.github.starwishsama.comet.utils.StringUtil.limitStringSize
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object BangumiCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        return when (args.getOrNull(0)) {
            "search" -> {
                val keyword = args.getOrNull(1) ?: return "未输入搜索词".toMessageChain()
                val firstItem = bgmCrawler.searchSubject(SearchType.Subject.All, keyword).items.firstOrNull()
                    ?: return "搜索不到与关键词有关的条目".toMessageChain()
                bgmCrawler.fetchSubject(firstItem.url).run {
                    buildString {
                        appendLine(title)
                        if (title != translatedTitle) appendLine("📖 中译 $translatedTitle")
                        append("🌟 评分 $score")
                        rank?.let { append(" | 🪜 排名 #$it") }
                        appendLine()
                        appendLine(summary?.limitStringSize(100) ?: "暂无简介")
                        append("标签: ")
                        if (tags.isEmpty()) {
                            append("暂无")
                        } else {
                            append(tags.joinToString(separator = " ", limit = 5, truncated = "...") { it.name })
                        }
                        appendLine()
                        append("🔗 查看更多 ${firstItem.url}")
                    }.toMessageChain()
                }
            }
            null -> "未输入子命令".toMessageChain()
            else -> "子命令错误".toMessageChain()
        }
    }

    override val props: CommandProps = CommandProps(
        name = "bangumi",
        aliases = arrayListOf("bgm"),
        description = "Bangumi 搜索查询指令",
        level = UserLevel.USER,
    )

    override fun getHelp(): String = """
        /bgm search <关键词> 搜索查看条目内容
    """.trimIndent()
}
