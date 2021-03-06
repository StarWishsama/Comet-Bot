package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote

object AutoReplyListener : NListener {
    override val eventToListen = listOf(GroupMessageEvent::class)

    override fun listen(event: Event) {
        if (event is GroupMessageEvent) {
            event.apply {
                val cfg = GroupConfigManager.getConfig(group.id)


                if (cfg?.keyWordReply == null || cfg.keyWordReply.isEmpty()) return

                val messageContent = message.contentToString()

                if (cfg.keyWordReply.isEmpty()) return

                cfg.keyWordReply.forEach {

                    if (it.keyWords.isEmpty()) return

                    it.keyWords.forEach { keyWord ->
                        if (messageContent.contains(keyWord)) {
                            runBlocking { subject.sendMessage(message.quote() + it.reply.toMessageChain(subject)) }
                            return
                        }
                    }
                }
            }
        }
    }

    override fun getName(): String = "关键词回复"
}