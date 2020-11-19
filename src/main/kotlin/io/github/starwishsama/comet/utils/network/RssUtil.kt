package io.github.starwishsama.comet.utils.network

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.github.starwishsama.comet.BotVariables.daemonLogger

object RssUtil {
    fun getFromEntry(entry: SyndEntry): String {
        return "${entry.title}\n-------------------------\n${entry.description.value}".trim()
    }

    fun getEntryFromURL(address: String): SyndEntry? {
        try {
            val entries = getRSSItems(address)
            return if (entries.isNotEmpty()) entries[0] else null
        } catch (e: Exception) {
            daemonLogger.warning("RSS | 在获取 RSS 信息时出现意外", e)
        }
        return null
    }

    fun simplifyHTML(raw: String): String {
        var context = raw
        context =
                context.replace("<br />".toRegex(), "\n").replace("<br>".toRegex(), "\n").replace("</p><p>".toRegex(), "\n")
                        .replace("	".toRegex(), "").replace("</li>".toRegex(), "\n")
        while (context.indexOf('<') != -1) {
            val l = context.indexOf('<')
            val r = context.indexOf('>')
            context = context.substring(0, l) + context.substring(r + 1)
        }
        while (context.contains("\n\n")) {
            context = context.replace("\n\n".toRegex(), "\n")
        }
        return context
    }

    private fun getRSSItems(address: String): List<SyndEntry> {
        return try {
            val response = NetUtil.executeHttpRequest(address, autoClose = true).body()
            val stream = response?.byteStream()

            if (response != null && stream != null) {
                val reader = XmlReader(stream)
                val input = SyndFeedInput()
                // 得到SyndFeed对象，即得到RSS源里的所有信息
                val feed: SyndFeed = input.build(reader)
                // 得到RSS源中子项列表
                feed.entries
            } else {
                emptyList()
            }
        } catch (t: RuntimeException) {
            if (!NetUtil.isTimeout(t)) {
                daemonLogger.warning("RSS | 在获取 RSS 信息时出现意外", t)
            }
            emptyList()
        }
    }
}