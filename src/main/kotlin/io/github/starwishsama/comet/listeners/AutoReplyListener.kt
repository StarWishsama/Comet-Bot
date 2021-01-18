package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.utils.warningS
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.MessageSource.Key.quote

object AutoReplyListener : NListener {
    override fun register(bot: Bot) {
        bot.eventChannel.subscribeGroupMessages {
            always {
                val cfg = GroupConfigManager.getConfig(group.id)

                try {
                    if (cfg?.keyWordReply == null || cfg.keyWordReply.isEmpty()) return@always

                    val messageContent = message.contentToString()

                    if (cfg.keyWordReply.isEmpty()) return@always

                    cfg.keyWordReply.forEach {

                        if (it.keyWords.isEmpty()) return@always

                        it.keyWords.forEach { keyWord ->
                            if (messageContent.contains(keyWord)) {
                                subject.sendMessage(message.quote() + it.reply.toMessageChain(subject))
                                return@always
                            }
                        }
                    }
                } catch (e: Exception) {
                    daemonLogger.warning("检测到群 ${group.id} 的配置文件异常.").also { daemonLogger.warningS(e) }
                    return@always
                }
            }
        }
    }

    override fun getName(): String = "关键词回复"
}