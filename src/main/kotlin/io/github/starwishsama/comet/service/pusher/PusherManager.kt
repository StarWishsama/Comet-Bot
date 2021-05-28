/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.pusher

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.BotVariables.mapper
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
    val pusherFolder = FileUtil.getChildFolder("pushers")
    private val usablePusher = mutableListOf<CometPusher>()

    fun initPushers(bot: Bot) {
        usablePusher.addAll(listOf(BiliDynamicPusher(bot), BiliLivePusher(bot), TwitterPusher(bot)))

        usablePusher.forEach { pusher ->
            try {
                val cfgFile = File(pusherFolder, "${pusher.name}.json")

                if (cfgFile.exists()) {
                    val cfg = mapper.readValue<PusherConfig>(cfgFile.getContext())
                    pusher.config = cfg
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
            it.save()
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