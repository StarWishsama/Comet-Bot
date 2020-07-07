package io.github.starwishsama.nbot.tasks

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.BotMain
import io.github.starwishsama.nbot.objects.pojo.Hitokoto
import io.github.starwishsama.nbot.utils.NetUtil

object HitokotoUpdater : Runnable {
    override fun run() {
        try {
            val hitokotoJson = NetUtil.getPageContent("https://v1.hitokoto.cn/")
            BotConstants.hitokoto = BotConstants.gson.fromJson(hitokotoJson, Hitokoto::class.java)
            BotMain.logger.info("已获取到今日一言")
        } catch (e: Throwable) {
            BotMain.logger.warning("在获取一言时发生了问题", e)
        }
    }

    fun getHitokoto(): String {
        try {
            val hitokoto = BotConstants.hitokoto
            return "\n今日一言:\n${hitokoto?.content} ——${hitokoto?.author}(${hitokoto?.source})"
        } catch (e: Exception) {
            BotMain.logger.error("在从缓存中获取一言时发生错误", e)
        }
        return "无法获取今日一言"
    }
}