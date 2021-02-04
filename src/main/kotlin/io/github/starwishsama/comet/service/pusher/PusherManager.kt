package io.github.starwishsama.comet.service.pusher

import io.github.starwishsama.comet.BotVariables.nullableGson
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
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

        usablePusher.forEach {
            val cfgFile = File(pusherFolder, "${it.name}.json")

            if (cfgFile.exists()) {
                val cfg = nullableGson.fromJson(cfgFile.getContext(), PusherConfig::class.java)
                it.config = cfg
                it.cachePool.addAll(cfg.cachePool)
            } else {
                cfgFile.createNewFile()
                cfgFile.writeClassToJson(it.config)
            }

            it.start()
        }
    }

    fun savePushers() {
        usablePusher.forEach {
            val cfgFile = File(pusherFolder, "${it.name}.json")

            if (!cfgFile.exists()) cfgFile.createNewFile()

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
}