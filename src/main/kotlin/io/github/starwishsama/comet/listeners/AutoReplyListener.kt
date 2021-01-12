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
                    BotVariables.daemonLogger.warning("检测到群 ${group.id} 的配置文件异常无法获取, 请及时查看!")
                    return@always
                }
            }
        }
    }

    override fun getName(): String = "关键词回复"
}