package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.managers.GroupConfigManager
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
                    cfg.keyWordReply.forEach {
                        it.keyWords.forEach { keyWord ->
                            if (messageContent.contains(keyWord)) {
                                subject.sendMessage(message.quote() + it.reply.toMessageChain(subject))
                                return@always
                            }
                        }

                        it.pattern.forEach { pattern ->
                            if (pattern.matcher(messageContent).find()) {
                                subject.sendMessage(message.quote() + it.reply.toMessageChain(subject))
                                return@always
                            }
                        }
                    }
                } catch (e: NullPointerException) {

                    BotVariables.daemonLogger.warning("检测到群 ${group.id} 的配置文件异常, 已修复!")
                    return@always
                }
            }
        }
    }

    override fun getName(): String = "关键词回复"
}