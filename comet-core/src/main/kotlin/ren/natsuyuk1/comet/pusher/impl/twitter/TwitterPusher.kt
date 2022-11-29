package ren.natsuyuk1.comet.pusher.impl.twitter

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.consts.json
import ren.natsuyuk1.comet.network.thirdparty.twitter.TwitterAPI
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.comet.pusher.CometPusher
import ren.natsuyuk1.comet.pusher.CometPusherConfig
import ren.natsuyuk1.comet.pusher.CometPusherContext
import ren.natsuyuk1.comet.utils.file.readTextBuffered
import ren.natsuyuk1.comet.utils.file.resolveDirectory
import ren.natsuyuk1.comet.utils.file.touch
import ren.natsuyuk1.comet.utils.file.writeTextBuffered
import java.io.File

private val logger = KotlinLogging.logger {}

object TwitterPusher : CometPusher("twitter", CometPusherConfig(300)) {
    private val subPath = File(resolveDirectory("./config/pusher/"), "${name}_sub.json")
    private val subscriber = mutableMapOf<String, MutableList<CometPushTarget>>()

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
        subscriber.forEach { (userID, target) ->
            try {
                val tweetResp = TwitterAPI.fetchTimeline(userID) ?: return@forEach

                if (tweetResp.tweets.isNullOrEmpty()) return@forEach

                val tweet = tweetResp.tweets!!.first()
                val id = tweet.id

                val isPushed = transaction {
                    CometPusherContext.isDuplicated(name, id)
                }

                if (!isPushed) {
                    val context = TwitterPushContext(id, target, tweet, tweetResp.includes)
                    pendingPushContext.add(context)
                    try {
                        transaction {
                            CometPusherContext.insertPushContext(name, context)
                        }
                    } catch (e: Exception) {
                        logger.warn(e) { "在写入推送数据至数据库时发生意外" }
                    }
                }
            } catch (e: Exception) {
                logger.warn(e) { "在尝试推送推特用户 $userID 的推文时出现问题" }
            }
        }

        logger.debug { "已获取 ${pendingPushContext.size} 个待推送推文." }
    }

    override suspend fun stop() {
        super.stop()

        subPath.touch()
        subPath.writeTextBuffered(json.encodeToString(subscriber))
    }
}
