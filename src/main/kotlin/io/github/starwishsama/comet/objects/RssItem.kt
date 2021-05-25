/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

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