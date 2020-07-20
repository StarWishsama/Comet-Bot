package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.utils.NetUtil

object HitokotoUpdater : Runnable {
    override fun run() {
        try {
            val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/")
            BotVariables.hitokoto = BotVariables.gson.fromJson(hitokotoJson, Hitokoto::class.java)
            BotVariables.logger.info("已获取到今日一言")
        } catch (e: Throwable) {
            BotVariables.logger.warning("在获取一言时发生了问题", e)
        }
    }

    fun getHitokoto(): String {
        try {
            val hitokoto = BotVariables.hitokoto
            return "\n今日一言:\n${hitokoto?.content} ——${hitokoto?.author}(${hitokoto?.source})"
        } catch (e: Exception) {
            BotVariables.logger.error("在从缓存中获取一言时发生错误", e)
        }
        return "无法获取今日一言"
    }
}