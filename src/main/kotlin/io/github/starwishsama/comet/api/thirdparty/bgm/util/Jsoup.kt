@file:Suppress("NOTHING_TO_INLINE")

package io.github.starwishsama.comet.api.thirdparty.bgm.util

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

internal inline infix fun Element?.byId(id: String): Element? = this?.getElementById(id)

internal inline infix fun Element?.byClass(clazz: String): Elements? = this?.getElementsByClass(clazz)

internal inline infix fun Elements?.byClass(clazz: String): Element? = this?.firstOrNull { it.hasClass(clazz) }

internal inline infix fun Elements?.byId(id: String): Element? = this?.firstOrNull { it.id() == id }

internal inline infix fun Elements?.byTag(tag: String): Elements? = this?.tagName(tag)
