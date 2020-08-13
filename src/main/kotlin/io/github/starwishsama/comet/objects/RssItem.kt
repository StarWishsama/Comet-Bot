package io.github.starwishsama.comet.objects

import cn.hutool.http.HtmlUtil
import com.rometools.rome.feed.synd.SyndEntry
import io.github.starwishsama.comet.utils.network.RssUtil

data class RssItem(val address: String, val switch: Boolean) {
    val subscribers: MutableSet<Long> = mutableSetOf()

    fun getContext(): String? {
        val entry = getEntry()
        if (entry != null) {
            return HtmlUtil.cleanHtmlTag(RssUtil.getFromEntry(entry))
        }
        return null
    }

    fun getEntry(): SyndEntry? {
        return RssUtil.getEntryFromURL(address)
    }
}