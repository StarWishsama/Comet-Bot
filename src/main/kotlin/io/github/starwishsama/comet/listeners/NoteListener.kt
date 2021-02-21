package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.wrapper.toMessageWrapper
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import kotlin.reflect.KClass

object NoteListener: NListener {
    override val eventToListen: List<KClass<out Event>> = listOf(GroupMessageEvent::class)

    override fun listen(event: Event) {
        if (event is GroupMessageEvent) {
            val msg = event.message

            var needToRecord = false

            for (sm in msg) {
                if (sm is At && sm.target == event.bot.id) {
                    needToRecord = true
                    break
                }
            }

            if (!needToRecord) return

            val quoteReply = msg.firstIsInstanceOrNull<QuoteReply>() ?: return

            val user = BotUser.getUser(event.sender.id) ?: return
            val content = user.savedContents

            content.add(quoteReply.source.originalMessage.toMessageWrapper())

            runBlocking {
                event.subject.sendMessage(QuoteReply(quoteReply.source) + "保存消息成功! 注意: 目前仅支持保存文本\n你可以在 /note list 中查看已保存的消息".sendMessage())
            }
        }
    }

    override fun getName(): String = "笔记"

}