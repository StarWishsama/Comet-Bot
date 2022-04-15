package io.github.starwishsama.comet.api.thirdparty.bgm.parser

import kotlinx.serialization.Serializable
import io.github.starwishsama.comet.api.thirdparty.bgm.Crawler
import io.github.starwishsama.comet.api.thirdparty.bgm.const.buildSubjectUrl
import io.github.starwishsama.comet.api.thirdparty.bgm.data.common.SearchType
import io.github.starwishsama.comet.api.thirdparty.bgm.data.resp.SearchTopicItem
import io.github.starwishsama.comet.api.thirdparty.bgm.util.byClass
import io.github.starwishsama.comet.api.thirdparty.bgm.util.byId
import io.github.starwishsama.comet.api.thirdparty.bgm.util.byTag
import moe.sdl.crawler.bgm.util.searchSubjectId
import moe.sdl.crawler.bgm.util.subjectVoteRegex
import org.jsoup.nodes.Element

@Serializable
class SubjectSearchResults(override val htmlPage: String) : Parser() {
  /**
   * 传入 ico_subject_type
   */
  private fun Element.parseCategory(): SearchType.Subject = when {
    hasClass("subject_type_1") -> SearchType.Subject.Book
    hasClass("subject_type_2") -> SearchType.Subject.Anime
    hasClass("subject_type_3") -> SearchType.Subject.Music
    hasClass("subject_type_4") -> SearchType.Subject.Game
    hasClass("subject_type_6") -> SearchType.Subject.Real
    else -> SearchType.Subject.All
  }

  private fun Element?.parsePreview(): SearchTopicItem? {
    val inner = this?.children()?.byClass("inner") ?: return null

    val h3 = inner.children().byTag("h3")?.firstOrNull() ?: return null

    val type = h3.parseCategory()

    val id = searchSubjectId.find(id())?.groupValues?.get(1)?.toLongOrNull() ?: return null

    val url = buildSubjectUrl(id)

    val coverUrl = children()
      .byClass("subjectCover")?.children()
      ?.byClass("image")
      ?.children()?.byTag("img")
      ?.attr("src")
      ?.let { "${Crawler.defaultProtocol.name}:$it" } ?: return null

    val zhTitle = h3.children().byClass("l")?.text() ?: return null
    val title = h3.children().byClass("grey")?.text() ?: return null

    val info = inner.children().byClass("info")?.text() ?: return null

    val rateInfo = inner.children().byClass("rateInfo")?.children()

    val rank = inner.children().byClass("rank")?.textNodes()?.firstOrNull()?.text()?.toIntOrNull()

    val score = rateInfo?.byClass("fade")?.text()?.toDoubleOrNull()
    val voteCount = rateInfo?.byClass("tip_j")?.text()?.let {
      subjectVoteRegex.find(it)?.groupValues?.get(1)?.toIntOrNull()
    }

    return SearchTopicItem(
      category = type,
      id = id,
      url = url,
      coverUrl = coverUrl,
      zhTitle = zhTitle,
      title = title,
      info = info,
      rank = rank,
      score = score,
      voteCount = voteCount,
    )
  }

  val items by lazy {
    dom.byId("browserItemList")?.children().orEmpty()
      .mapNotNull { it.parsePreview() }
  }
}
