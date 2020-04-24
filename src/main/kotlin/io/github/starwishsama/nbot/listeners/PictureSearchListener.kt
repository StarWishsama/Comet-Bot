package io.github.starwishsama.nbot.listeners

import io.github.starwishsama.nbot.enums.SessionType
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.PictureSearchUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*

object PictureSearchListener {
    fun register(bot : Bot){
        bot.logger.info("[监听器] 已注册 会话 监听器")
        bot.subscribeGroupMessages {
            always {
                if (SessionManager.isValidSession(sender.id)){
                    val session = SessionManager.getSession(sender.id)
                    if (session?.type == SessionType.DELAY){
                        SessionManager.expireSession(sender.id)
                        try {
                            val image = this[Image]
                            reply(run {
                                if (image.isNotEmpty()) {
                                    reply("请稍等...")
                                    val result = PictureSearchUtil.sauceNaoSearch(image.queryUrl())
                                    if (result.similarity >= 60.0) {
                                        /**val map = mutableMapOf<String, String>()
                                        map["user-agent"] =
                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"
                                        val stream = HttpRequest.get(result.picUrl)
                                            .setFollowRedirects(true)
                                            .timeout(150_000)
                                            .addHeaders(map)
                                            .execute().bodyStream()*/
                                        "相似度:${result.similarity}%\n原图链接:${result.originalUrl}\n".toMessage()
                                            .asMessageChain()/**.plus(stream.uploadAsImage(subject).asMessageChain())*/
                                    } else {
                                        "相似度过低 (${result.similarity}%), 请尝试更换图片重试".toMessage().asMessageChain()
                                    }
                                } else {
                                    EmptyMessageChain
                                }
                            })
                        } catch (e: NoSuchElementException){
                            reply("无法识别图片")
                        }
                    }
                }
            }
        }
    }
}