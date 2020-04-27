package io.github.starwishsama.nbot.objects

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.FeedException
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.io.IOException
import java.net.URL


class RssItem(private val address: String) {
    val ifEnabled = false
    val subscribers: List<Long>? = null
    val context: String
        get() = simplifyHTML(getFromURL(address))

    val title: String
        get() = simplifyHTML(getTitleFromURL(address))

    val entry: SyndEntry?
        get() = getEntryFromURL(address)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
        }

        // 此函数仅供内部调用，正常情况下不应调用
        private fun getFromURL(address: String): String {
            return try {
                val url = URL(address)
                // 读取RSS源
                val reader = XmlReader(url)
                val input = SyndFeedInput()
                // 得到SyndFeed对象，即得到RSS源里的所有信息
                val feed: SyndFeed = input.build(reader)
                // 得到Rss新闻中子项列表
                val entries = feed.entries
                val entry = entries[0]
                entry.title + "\n" + "-------------------------\n" + entry.description.value.trim { it <= ' ' }
            } catch (e: Exception) {
                e.printStackTrace()
                "Encountered a wrong URL or a network error."
            }
        }

        private fun getEntryFromURL(address: String): SyndEntry? {
            try {
                val url = URL(address)
                // 读取RSS源
                val reader = XmlReader(url)
                val input = SyndFeedInput()
                // 得到SyndFeed对象，即得到RSS源里的所有信息
                val feed: SyndFeed = input.build(reader)
                // 得到Rss新闻中子项
                return feed.entries[0]
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun getTitleFromURL(address: String): String {
            return try {
                val url = URL(address)
                val reader = XmlReader(url)
                val input = SyndFeedInput()
                // 得到SyndFeed对象，即得到RSS源里的所有信息
                val feed: SyndFeed = input.build(reader)
                // 得到Rss新闻中子项列表
                val entries = feed.entries
                val entry = entries[0]
                entry.title
            } catch (e: Exception) {
                e.printStackTrace()
                "Encountered a wrong URL or a network error."
            }
        }

        fun simplifyHTML(rssContext: String): String {
            var context = rssContext
            context = context.replace("<br />".toRegex(), "\n").replace("<br>".toRegex(), "\n")
                .replace("</p><p>".toRegex(), "\n")
                .replace("	".toRegex(), "")
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

        fun getRSSItem(address: String?): SyndEntry? {
            try {
                val url = URL(address)
                val reader = XmlReader(url)
                val input = SyndFeedInput()
                // 得到SyndFeed对象，即得到RSS源里的所有信息
                val feed: SyndFeed = input.build(reader)
                // 得到RSS源中子项列表
                return feed.entries[0]
            } catch (e: FeedException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        fun getRSSItems(address: String?): List<SyndEntry>? {
            return try {
                val url = URL(address)
                val reader = XmlReader(url)
                val input = SyndFeedInput()
                // 得到SyndFeed对象，即得到RSS源里的所有信息
                val feed: SyndFeed = input.build(reader)
                // 得到RSS源中子项列表
                feed.entries
            } catch (e: FeedException) {
                e.printStackTrace()
                null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

}