package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.MessageSource.Key.quote

object AutoReplyListener : NListener {
    override fun register(bot: Bot) {
        bot.eventChannel.subscribeGroupMessages {
            always {
                try {
                    val cfg = GroupConfigManager.getConfigOrNew(group.id)

                    if (cfg.keyWordReply.isEmpty()) return@always

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
                    return@always
                }
            }
        }
    }

    override fun getName(): String = "关键词回复"
}