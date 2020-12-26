package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages

object AutoReplyListener : NListener {
    override fun register(bot: Bot) {
        bot.eventChannel.subscribeGroupMessages {
            always {
                val cfg = GroupConfigManager.getConfig(group.id) ?: return@always

                if (cfg.keyWordReply.isEmpty()) return@always

                val messageContent = message.contentToString()
                cfg.keyWordReply.forEach {
                    it.keyWords.forEach { keyWord ->
                        if (messageContent.contains(keyWord)) {
                            quoteReply(it.reply.toMessageChain(subject))
                            return@always
                        }
                    }

                    it.pattern.forEach { pattern ->
                        if (pattern.matcher(messageContent).find()) {
                            quoteReply(it.reply.toMessageChain(subject))
                            return@always
                        }
                    }
                }
            }
        }
    }

    override fun getName(): String = "关键词回复"
}