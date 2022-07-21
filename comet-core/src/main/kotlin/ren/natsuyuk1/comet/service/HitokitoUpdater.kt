package ren.natsuyuk1.comet.service

import io.ktor.client.request.*
import ren.natsuyuk1.comet.consts.cometClient
import ren.natsuyuk1.comet.objects.hitokito.Hitokoto
import java.io.IOException

private val logger = mu.KotlinLogging.logger {}

object HitokotoManager {
    private var hitokoto: Hitokoto? = null

    suspend fun fetch() {
        try {
            hitokoto = cometClient.client.get<Hitokoto>("https://v1.hitokoto.cn/")
            logger.info { "已获取到今日一言" }
        } catch (e: IOException) {
            logger.warn(e) { "在获取一言时发生了问题" }
        }
    }

    suspend fun getHitokoto(useCache: Boolean = true): String {
        try {
            return if (useCache) {
                hitokoto.toString()
            } else {
                cometClient.client.get<Hitokoto>("https://v1.hitokoto.cn/").toString()
            }
        } catch (e: IOException) {
            logger.warn(e) { "获取一言时发生错误" }
        }
        return "无法获取今日一言"
    }
}
