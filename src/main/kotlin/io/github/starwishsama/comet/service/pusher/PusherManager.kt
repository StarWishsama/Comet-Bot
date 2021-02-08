package io.github.starwishsama.comet.service.pusher

import com.github.salomonbrys.kotson.fromJson
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.context.BiliBiliDynamicContext
import io.github.starwishsama.comet.service.pusher.instances.BiliDynamicPusher
import io.github.starwishsama.comet.service.pusher.instances.BiliLivePusher
import io.github.starwishsama.comet.service.pusher.instances.TwitterPusher
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.getContext
import io.github.starwishsama.comet.utils.writeClassToJson
import net.mamoe.mirai.Bot
import java.io.File

object PusherManager {
    private val pusherFolder = FileUtil.getChildFolder("pushers")
    private val usablePusher = mutableListOf<CometPusher>()

    fun initPushers(bot: Bot) {
        usablePusher.addAll(listOf(BiliDynamicPusher(bot), BiliLivePusher(bot), TwitterPusher(bot)))

        usablePusher.forEach { pusher ->
            try {
                val cfgFile = File(pusherFolder, "${pusher.name}.json")

                if (cfgFile.exists()) {
                    val cfg = nullableGson.fromJson<PusherConfig>(cfgFile.getContext())
                    pusher.config = cfg
                    pusher.cachePool.addAll(cfg.cachePool)
                    initBiliBiliDynamic(pusher)
                } else {
                    cfgFile.createNewFile()
                    cfgFile.writeClassToJson(pusher.config)
                }

                pusher.start()
            } catch (e: Exception) {
                daemonLogger.warning("在初始化推送器 ${pusher.name} 时遇到了问题", e)
            }
        }
    }

    fun savePushers() {
        usablePusher.forEach {
            val cfgFile = File(pusherFolder, "${it.name}.json")

            if (!cfgFile.exists()) cfgFile.createNewFile()

            it.config.cachePool.addAll(it.cachePool)

            cfgFile.writeClassToJson(it.config)
        }
    }

    fun getPushers(): MutableList<CometPusher> {
        return usablePusher
    }

    fun getPusherByName(name: String): CometPusher? {
        getPushers().forEach {
            if (it.name == name) {
                return it
            }
        }

        return null
    }

    /**
     * 初始化缓存中的哔哩哔哩动态历史, 避免缓存大小过大
     */
    private fun initBiliBiliDynamic(pusher: CometPusher) {
        if (pusher is BiliDynamicPusher) {
            pusher.cachePool.forEach {
                if (it is BiliBiliDynamicContext) {
                    it.initDynamic()
                }
            }
        }
    }
}