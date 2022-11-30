package ren.natsuyuk1.comet.telegram.event

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onGroupEvent
import dev.inmo.tgbotapi.extensions.utils.asGroupChat
import dev.inmo.tgbotapi.types.message.ChatEvents.LeftChatMemberEvent
import dev.inmo.tgbotapi.types.message.ChatEvents.NewChatMembers
import dev.inmo.tgbotapi.utils.PreviewFeature
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.group.GroupJoinEvent
import ren.natsuyuk1.comet.api.event.events.group.GroupLeaveEvent
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.contact.toCometGroup
import ren.natsuyuk1.comet.telegram.contact.toCometGroupMember

@OptIn(PreviewFeature::class)
suspend fun BehaviourContext.listenGroupEvent(comet: TelegramComet) {
    onGroupEvent {
        when (it.chatEvent) {
            is NewChatMembers -> {
                (it.chatEvent as NewChatMembers).members.forEach { user ->
                    GroupJoinEvent.Normal(
                        user.toCometGroupMember(comet, it.chat.id)
                    ).broadcast()
                }
            }

            is LeftChatMemberEvent -> {
                it.chat.asGroupChat()?.toCometGroup(comet)?.let { it1 ->
                    GroupLeaveEvent(
                        comet,
                        it1,
                        (it.chatEvent as LeftChatMemberEvent).user.toCometGroupMember(comet, it.chat.id)
                    ).broadcast()
                }
            }
        }
    }
}
