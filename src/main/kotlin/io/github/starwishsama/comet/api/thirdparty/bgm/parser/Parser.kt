package io.github.starwishsama.comet.api.thirdparty.bgm.parser

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


@Serializable
sealed class Parser {
  @Required
  protected abstract val htmlPage: String

  protected val dom: Document by lazy {
    Jsoup.parse(htmlPage)
  }
}
