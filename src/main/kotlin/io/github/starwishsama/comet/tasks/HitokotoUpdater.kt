package io.github.starwishsama.comet.tasks

import io.github.starwishsama.comet.BotConstants
import io.github.starwishsama.comet.Comet
import io.github.starwishsama.comet.objects.pojo.Hitokoto
import io.github.starwishsama.comet.utils.NetUtil

object HitokotoUpdater : Runnable {
    override fun run() {
        try {
            val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/")
            BotConstants.hitokoto = BotConstants.gson.fromJson(hitokotoJson, Hitokoto::class.java)
            Comet.logger.info("已获取到今日一言")
        } catch (e: Throwable) {
            Comet.logger.warning("在获取一言时发生了问题", e)
        }
    }

    fun getHitokoto(): String {
        try {
            val hitokoto = BotConstants.hitokoto
            return "\n今日一言:\n${hitokoto?.content} ——${hitokoto?.author}(${hitokoto?.source})"
        } catch (e: Exception) {
            Comet.logger.error("在从缓存中获取一言时发生错误", e)
        }
        return "无法获取今日一言"
    }
}