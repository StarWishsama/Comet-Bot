@file:Suppress("NOTHING_TO_INLINE")

package ren.natsuyuk1.comet.utils.jsoup

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

inline infix fun Element?.byId(id: String): Element? = this?.getElementById(id)

inline infix fun Element?.byClass(clazz: String): Elements? = this?.getElementsByClass(clazz)

inline infix fun Elements?.byClass(clazz: String): Element? = this?.firstOrNull { it.hasClass(clazz) }

inline infix fun Elements?.byId(id: String): Element? = this?.firstOrNull { it.id() == id }

inline infix fun Elements?.byTag(tag: String): Elements? = this?.tagName(tag)
