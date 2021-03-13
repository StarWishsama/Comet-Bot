package io.github.starwishsama.comet.service.pusher.instances

import cn.hutool.core.util.RandomUtil
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.twitter.TwitterApi
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.service.pusher.CometPusher
import io.github.starwishsama.comet.service.pusher.PusherManager
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.*
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.writeClassToJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class TwitterPusher(bot: Bot): CometPusher(bot, "twitter") {
    override var config: PusherConfig = PusherConfig(30_0000L)

    private val cachePool: MutableList<TwitterContext> = mutableListOf()

    override fun retrieve() {
        GroupConfigManager.getAllConfigs().parallelStream().forEach { cfg ->
            if (cfg.twitterPushEnabled) {
                cfg.twitterSubscribers.forEach tweet@ { user ->
                    val cache = cachePool.getTwitterContext(user)

                    if (cache?.status == PushStatus.READY) {
                        cache.addPushTarget(cfg.id)
                        return@tweet
                    }

                    val latestTweet = TwitterApi.getTweetInTimeline(user, 0, 2) ?: return@tweet
                    val time = System.currentTimeMillis()

                    val current = TwitterContext(retrieveTime = time, status = PushStatus.READY, twitterUserName = user, tweetId = latestTweet.id)

                    if (cache == null) {
                        cachePool.add(current.also { it.addPushTarget(cfg.id) })
                        addRetrieveTime()
                    } else if (!cache.contentEquals(current)) {
                        cache.apply {
                            this.retrieveTime = time
                            this.tweetId = latestTweet.id
                            this.status = PushStatus.READY
                            addPushTarget(cfg.id)
                        }
                        addRetrieveTime()
                    }
                }
            }
        }

        if (retrieveTime > 0) {
            BotVariables.daemonLogger.verbose("已获取了 $retrieveTime 条推文")
            resetRetrieveTime()
        }
    }

    override fun push() {
        cachePool.forEach { context ->
            if (context.status == PushStatus.READY) {
                context.getPushTarget().forEach group@{
                    try {
                        val wrapper = context.toMessageWrapper()

                        if (wrapper.isUsable()) {
                            val group = bot.getGroup(it) ?: return@group

                            runBlocking {
                                group.sendMessage(wrapper.toMessageChain(group))
                                delay(RandomUtil.randomLong(1000, 2000))
                            }

                            addPushTime()
                        }
                    } catch (e: Exception) {
                        BotVariables.daemonLogger.warning("在推送开播消息至群 $it 时出现异常", e)
                    }
                }

                context.clearPushTarget()
                context.status = PushStatus.FINISHED
            }
        }

        if (pushTime > 0) {
            BotVariables.daemonLogger.verbose("$name 已成功推送 $pushTime 个消息")
            resetPushTime()
        }

        latestPushTime = LocalDateTime.now()
        save()
    }

    override fun save() {
        val cfgFile = File(PusherManager.pusherFolder, "${name}.json")

        if (!cfgFile.exists()) cfgFile.createNewFile()

        config.cachePool = BotVariables.mapper.writeValueAsString(cachePool)

        cfgFile.writeClassToJson(config)
    }

    override fun start() {
        if (config.cachePool.isNotEmpty()) {
            try {
                cachePool.addAll(BotVariables.mapper.readValue(config.cachePool))
            } catch (e: Exception) {
                BotVariables.daemonLogger.warning("无法解析 $name 历史推送记录")
            }
        }
        TaskUtil.runScheduleTaskAsync(config.interval, config.interval, TimeUnit.MILLISECONDS) {
            execute()
        }

        BotVariables.daemonLogger.info("$name 推送器已启动, 载入缓存 ${cachePool.size} 个")
    }
}