package io.github.starwishsama.comet.listeners

import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.wrapper.AtElement
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.objects.wrapper.PureText
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.MemberJoinEvent
import kotlin.reflect.KClass

object GroupMemberChangedListener: NListener {
    override val eventToListen: List<KClass<out Event>> = listOf(MemberJoinEvent::class)

    override fun listen(event: Event) {
        if (event is MemberJoinEvent) {
            val cfg = GroupConfigManager.getConfig(event.groupId) ?: return

            if (cfg.newComerWelcome && !cfg.newComerWelcomeText.isEmpty()) {
                runBlocking { event.group.sendMessage(reWrapMessage(cfg.newComerWelcomeText, event).toMessageChain(event.group)) }
            }
        }
    }

    override fun getName(): String = "群聊欢迎"

    private fun reWrapMessage(original: MessageWrapper, event: MemberJoinEvent): MessageWrapper {
        val newWrapper = MessageWrapper()

        for (wrapperElement in original.getMessageContent()) {
            if (wrapperElement is PureText) {
                val index = wrapperElement.text.indexOf("[At]")
                if (index > -1) {
                    val before = wrapperElement.text.substring(0, index)
                    val after = wrapperElement.text.substring(index)

                    newWrapper.addText("$before ")
                    newWrapper.addElement(AtElement(event.member.id))
                    newWrapper.addText(" $after")
                } else {
                    newWrapper.addElement(wrapperElement)
                }
            } else {
                newWrapper.addElement(wrapperElement)
            }
        }

        return newWrapper
    }
}