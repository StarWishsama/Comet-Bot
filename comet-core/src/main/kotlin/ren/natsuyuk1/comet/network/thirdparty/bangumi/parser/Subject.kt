package ren.natsuyuk1.comet.network.thirdparty.bangumi.parser

import kotlinx.serialization.Serializable
import org.jsoup.nodes.Element
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.network.thirdparty.bangumi.const.MAIN_DOMAIN
import ren.natsuyuk1.comet.network.thirdparty.bangumi.data.resp.*
import ren.natsuyuk1.comet.network.thirdparty.bangumi.util.*
import ren.natsuyuk1.comet.utils.jsoup.byClass
import ren.natsuyuk1.comet.utils.jsoup.byId
import ren.natsuyuk1.comet.utils.jsoup.byTag
import ren.natsuyuk1.comet.utils.string.StringUtil.limit

@Serializable
class Subject(override val htmlPage: String) : Parser(), Unique {

    private val nameSingle by lazy {
        dom.byId("headerSubject")
            .byClass("nameSingle")
            ?.firstOrNull()?.children().byTag("a")
            ?.firstOrNull { it.hasAttr("property") && it.hasAttr("href") }
    }

    override val id: Long by lazy {
        val href = nameSingle?.attr("href") ?: error("Failed to parse id")
        subjectIdRegex.find(href)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: error("Failed to parse id")
    }

    val title: String? by lazy {
        nameSingle?.text()
    }

    val translatedTitle: String? by lazy {
        nameSingle?.attr("title")
    }

    private val rating by lazy {
        dom.byId("panelInterestWrapper")
            .byClass("SidePanel")?.firstOrNull()?.children()
            ?.firstOrNull { it.children().size > 0 }
            ?.attr("rel", "v:rating")
    }

    private val globalScore by lazy {
        rating?.children()?.byClass("global_rating")
            ?.byClass("global_score")?.firstOrNull()?.children()
    }

    val score: Double? by lazy {
        globalScore
            ?.attr("property", "v:average")?.firstOrNull()
            ?.text()?.toDoubleOrNull()
    }

    val rank: Int? by lazy {
        val text = globalScore?.firstOrNull { it.childrenSize() > 0 }
            ?.children()?.byClass("alarm")?.text()
            ?: return@lazy null
        subjectRankRegex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private val scoreChart by lazy {
        rating?.children().byId("ChartWarpper") // not typo
    }

    val totalVote: Int? by lazy {
        scoreChart
            ?.children().byClass("chart_desc")
            ?.children().byClass("grey")
            ?.children()?.attr("property", "v:votes")
            ?.text()
            ?.toIntOrNull()
    }

    /**
     * map, 评分 to 人数
     */
    val voteChart: Map<Int, Int?> by lazy {
        scoreChart
            ?.children().byClass("horizontalChart")
            ?.children()?.filter { ele -> ele?.tagName() == "li" }
            ?.map { it?.children()?.tagName("a")?.firstOrNull() }
            ?.foldIndexed(mutableMapOf<Int, Int?>()) { idx, acc, i ->
                i?.attr("title")?.also {
                    acc[10 - idx] = subjectVoteRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull()
                }
                acc
            }.orEmpty()
    }

    private val subjectDetail by lazy {
        dom.byId("subject_detail")
    }

    val summary: String? by lazy {
        subjectDetail?.children().byId("subject_summary")
            ?.textNodes()?.joinToString("") { it.wholeText }
    }

    /**
     * tag 列表, 会按照 tag 数量降序排列
     */
    val tags: List<Tag> by lazy {
        subjectDetail?.children().byClass("subject_tag_section")
            ?.children()?.byClass("inner")
            ?.children()?.asSequence()
            ?.filter { it.tagName() == "a" }
            ?.map { it: Element? ->
                Tag(
                    name = it?.children()?.byTag("span")?.firstOrNull()?.text() ?: return@map null,
                    url = MAIN_DOMAIN + it.attr("href"),
                    count = it.children().byClass("grey")?.text()?.toIntOrNull() ?: return@map null
                )
            }?.filterNotNull().orEmpty().toList()
    }

    val tagByName: Map<String, Tag> by lazy {
        tags.associateBy { it.name }
    }

    private val episodeBox by lazy {
        subjectDetail?.children().byClass("subject_prg")
    }

    val episodesUrl by lazy {
        episodeBox
            ?.children()?.byClass("tip")
            ?.nextElementSibling()
            ?.attr("href")
            ?.let { "$MAIN_DOMAIN$it" }
    }

    val simpleEpisodes by lazy {
        var category: String? = null
        episodeBox?.children()
            ?.byClass("prg_list")?.children().orEmpty()
            .asSequence()
            .filterNotNull()
            .onEach {
                if (it.hasClass("subtitle")) {
                    category = it.parseCategory()
                }
            }.map {
                it.parseNormalEpisode(category)
            }.filterNotNull().toList()
    }

    // 需要传入包含的 <li></li> 一层
    private fun Element.parseNormalEpisode(category: String?): SimpleEpisode? =
        children().firstOrNull { it.tagName() == "a" }.let {
            val idMatch = episodeIdRegex.find(it?.attr("href") ?: return@let null)
            val id = idMatch?.groupValues?.getOrNull(1)?.toLongOrNull()
            SimpleEpisode(
                id = id ?: return@let null,
                title = it.text(),
                longTitle = it.attr("title"),
                url = MAIN_DOMAIN + it.attr("href"),
                category = category
            )
        }

    private fun Element.parseCategory(): String? = children().firstOrNull { it.tagName() == "span" }?.text()

    val followStatus by lazy {
        dom.byId("subjectPanelCollect")
            ?.children()?.byClass("tip_i")
            ?.children().orEmpty().asSequence()
            .map {
                val text = it.text()
                val num = startNumRegex.find(text)?.groupValues?.getOrNull(1)
                val type = FollowType.parse(text)
                val path: String = it.attr("href")
                FollowStatus(
                    num = num ?: return@map null,
                    link = MAIN_DOMAIN + path,
                    type = type ?: return@map null
                )
            }.filterNotNull().toList()
    }
}

fun Subject.toMessageWrapper(url: String) =
    buildMessageWrapper {
        title?.let { appendTextln(it) }
        if (title != translatedTitle) appendTextln("📖 中译 $translatedTitle")
        appendText("🌟 评分 $score")
        rank?.let { appendText(" | 🪜 排名 #$it") }
        appendLine()
        appendText(summary?.limit(100) ?: "暂无简介")
        appendLine()
        appendText("标签: ")
        if (tags.isEmpty()) {
            appendText("暂无")
        } else {
            appendText(tags.joinToString(separator = " ", limit = 5, truncated = "...") { it.name })
        }
        appendLine()
        appendText("🔗 查看更多 $url")
    }
