package io.github.starwishsama.comet.service.pusher.instances

import cn.hutool.core.util.RandomUtil
import com.github.salomonbrys.kotson.fromJson
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.gson
import io.github.starwishsama.comet.api.thirdparty.bilibili.DynamicApi
import io.github.starwishsama.comet.api.thirdparty.bilibili.data.dynamic.Dynamic
import io.github.starwishsama.comet.exceptions.ApiException
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

class BiliDynamicPusher(bot: Bot) : CometPusher(bot, "bili_dynamic") {
    override var config: PusherConfig = PusherConfig(30_0000L)

    private val cachePool: MutableList<BiliBiliDynamicContext> = mutableListOf()

    override fun retrieve() {
        GroupConfigManager.getAllConfigs().forEach { cfg ->
            if (cfg.biliPushEnabled) {
                cfg.biliSubscribers.forEach user@{ user ->
                    val dynamic: Dynamic = try {
                        DynamicApi.getUserDynamicTimeline(user.id.toLong())
                    } catch (e: RuntimeException) {
                        if (e !is ApiException) {
                            daemonLogger.warning("在获取动态时出现了异常", e)
                        }
                        null
                    } ?: return@user
                    val time = System.currentTimeMillis()
                    val cache = cachePool.getDynamicContext(user.id.toLong())
                    val current = BiliBiliDynamicContext(
                        mutableListOf(cfg.id),
                        time,
                        pushUser = user,
                        dynamicId = dynamic.data.cards?.get(0)?.description?.dynamicId ?: -1
                    )

                    if (cache == null) {
                        cachePool.add(current)
                        addRetrieveTime()
                    } else if (!cache.contentEquals(current)) {
                        cache.apply {
                            this.retrieveTime = time
                            this.dynamicId = dynamic.data.cards?.get(0)?.description?.dynamicId ?: -1
                            this.status = PushStatus.READY
                            addPushTarget(cfg.id)
                        }

                        addRetrieveTime()
                    }
                }
            }
        }

        if (retrieveTime > 0) {
            daemonLogger.verbose("已获取了 $retrieveTime 个动态")
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
                        daemonLogger.warning("在推送开播消息至群 $it 时出现异常", e)
                    }
                }

                context.clearPushTarget()
                context.status = PushStatus.FINISHED
            }
        }

        if (pushTime > 0) {
            daemonLogger.verbose("$name 已成功推送 $pushTime 个消息")
            resetPushTime()
        }

        latestPushTime = LocalDateTime.now()
        save()
    }

    override fun save() {
        val cfgFile = File(PusherManager.pusherFolder, "${name}.json")

        if (!cfgFile.exists()) cfgFile.createNewFile()

        config.cachePool = gson.toJson(cachePool)

        cfgFile.writeClassToJson(config)
    }

    override fun start() {
        if (config.cachePool.isNotEmpty()) {
            try {
                cachePool.addAll(gson.fromJson(config.cachePool))
            } catch (e: Exception) {
                daemonLogger.warning("无法解析 $name 历史推送记录")
            }
        }
        TaskUtil.runScheduleTaskAsync(config.interval, config.interval, TimeUnit.MILLISECONDS) {
            execute()
        }

        daemonLogger.info("$name 推送器已启动, 载入缓存 ${cachePool.size} 个")
    }
}