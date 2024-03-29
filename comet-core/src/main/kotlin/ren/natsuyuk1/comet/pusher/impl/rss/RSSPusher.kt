package ren.natsuyuk1.comet.pusher.impl.rss

import cn.hutool.crypto.SecureUtil
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.comet.pusher.CometPusher
import ren.natsuyuk1.comet.pusher.CometPusherConfig
import ren.natsuyuk1.comet.pusher.CometPusherContext
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File
import java.io.IOException
import java.io.InputStream

private val logger = KotlinLogging.logger {}

object RSSPusher : CometPusher("RSS", CometPusherConfig(60)) {
    private val subPath = File(resolveDirectory("./config/pusher/"), "${name}_sub.json")
    val subscriber = mutableMapOf<String, MutableList<CometPushTarget>>()

    override fun init() {
        super.init()

        runBlocking {
            if (!subPath.exists()) {
                subPath.touch()
            } else {
                subPath.readTextBuffered().let {
                    if (it.isNotEmpty()) {
                        subscriber.putAll(json.decodeFromString(it))
                    }
                }
            }
        }
    }

    override suspend fun retrieve() {
        subscriber.forEach { (rssURL, target) ->
            try {
                val body = cometClient.client
                    .get(rssURL)
                    .body<InputStream>()
                val feed: SyndFeed = SyndFeedInput().build(XmlReader(body))
                val feedID = SecureUtil.md5(feed.description)

                val isPushed = transaction {
                    CometPusherContext.isDuplicated(name, feedID)
                }

                if (!isPushed) {
                    val context = RSSPushContext(feedID, target, feed.entries.first())
                    pendingPushContext.add(context)
                    try {
                        transaction {
                            CometPusherContext.insertPushContext(name, context)
                        }
                    } catch (e: Exception) {
                        logger.warn(e) { "在写入推送数据至数据库时发生意外" }
                    }
                }
            } catch (e: IOException) {
                logger.warn(e) { "在推送 RSS 源 ($rssURL) 时出现问题" }
            }
        }

        logger.debug { "已获取 ${pendingPushContext.size} 个待推送 RSS 内容." }
    }

    override suspend fun stop() {
        super.stop()

        subPath.touch()
        subPath.writeTextBuffered(json.encodeToString(subscriber))
    }
}
