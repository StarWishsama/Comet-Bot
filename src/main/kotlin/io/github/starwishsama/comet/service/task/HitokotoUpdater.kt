/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.task

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.utils.network.NetUtil
import java.io.IOException

object HitokotoUpdater : Runnable {
    private var hitokoto: Hitokoto? = null

    override fun run() {
        try {
            val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/")
            if (hitokotoJson != null) {
                hitokoto = getHitokotoJson()
                logger.verbose("已获取到今日一言")
            }
        } catch (e: RuntimeException) {
            logger.warning("在获取一言时发生了问题\n${e.stackTraceToString()}")
        }
    }

    private fun getHitokotoJson(): Hitokoto {
        val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/") ?: throw ApiException("在获取一言时发生了问题")
        return BotVariables.mapper.readValue(hitokotoJson, Hitokoto::class.java)
    }

    fun getHitokoto(useCache: Boolean = true): String {
        try {
            return if (useCache) {
                hitokoto.toString()
            } else {
                getHitokotoJson().toString()
            }
        } catch (e: IOException) {
            logger.warning("在从缓存中获取一言时发生错误", e)
        }
        return "无法获取今日一言"
    }
}