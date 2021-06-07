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

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.service.pusher.config.PusherConfig
import io.github.starwishsama.comet.service.pusher.instances.BiliBiliDynamicPusher
import io.github.starwishsama.comet.service.pusher.instances.BiliBiliLivePusher
import io.github.starwishsama.comet.service.pusher.instances.CometPusher
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
        usablePusher.addAll(listOf(BiliBiliDynamicPusher(bot), BiliBiliLivePusher(bot), TwitterPusher(bot)))

        usablePusher.forEach { pusher ->
            val cfgFile = File(pusherFolder, "${pusher.name}.json")

            try {

                if (cfgFile.exists()) {
                    val cfg = BotVariables.mapper.readValue<PusherConfig>(cfgFile.getContext())
                    pusher.config = cfg
                } else {
                    cfgFile.createNewFile()
                    cfgFile.writeClassToJson(pusher.config)
                }

                pusher.start()
            } catch (e: Exception) {
                if (e is JsonProcessingException || e is JsonMappingException) {
                    BotVariables.daemonLogger.warning("在解析推送器配置 ${pusher.name} 时遇到了问题, 已自动重新生成", e)
                    cfgFile.delete()
                    cfgFile.createNewFile().also { cfgFile.writeClassToJson(pusher.config) }
                } else {
                    BotVariables.daemonLogger.warning("在初始化推送器 ${pusher.name} 时遇到了问题", e)
                }
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