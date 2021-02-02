package io.github.starwishsama.comet.service.oldpushers

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.hitokoto
import io.github.starwishsama.comet.BotVariables.logger
import io.github.starwishsama.comet.exceptions.ApiException
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.utils.network.NetUtil
import io.github.starwishsama.comet.utils.verboseS

object HitokotoUpdater : Runnable {
    override fun run() {
        try {
            val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/")
            if (hitokotoJson != null) {
                hitokoto = getHitokotoJson()
                logger.verboseS("已获取到今日一言")
            }
        } catch (e: RuntimeException) {
            logger.warning("在获取一言时发生了问题\n${e.stackTraceToString()}")
        }
    }

    private fun getHitokotoJson(): Hitokoto {
        val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/") ?: throw ApiException("在获取一言时发生了问题")
        return BotVariables.nullableGson.fromJson(hitokotoJson, Hitokoto::class.java)
    }

    fun getHitokoto(useCache: Boolean = true): String {
        try {
            return if (useCache) {
                hitokoto.toString()
            } else {
                getHitokotoJson().toString()
            }
        } catch (e: Exception) {
            logger.warning("在从缓存中获取一言时发生错误", e)
        }
        return "无法获取今日一言"
    }
}