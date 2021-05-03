package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
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

                val user = BotUser.getUserOrRegister(sender.id)

                val currentTime = System.currentTimeMillis()

                val hasCoolDown = when (user.lastExecuteTime) {
                    -1L -> {
                        user.lastExecuteTime = currentTime
                        true
                    }
                    else -> {
                        val result = currentTime - user.lastExecuteTime < 5000
                        user.lastExecuteTime = currentTime
                        result
                    }
                }

                if (!hasCoolDown) return

                val messageContent = message.contentToString()

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