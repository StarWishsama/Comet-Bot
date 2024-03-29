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
import ren.natsuyuk1.comet.telegram.contact.toCometUser

@OptIn(PreviewFeature::class)
suspend fun BehaviourContext.listenGroupEvent(comet: TelegramComet) {
    onGroupEvent {
        when (val event = it.chatEvent) {
            is NewChatMembers -> {
                event.members.forEach { user ->
                    GroupJoinEvent.Normal(
                        user.toCometGroupMember(comet, it.chat.id),
                    ).broadcast()
                }
            }

            is LeftChatMemberEvent -> {
                it.chat.asGroupChat()?.toCometGroup(comet)?.let { group ->
                    GroupLeaveEvent(
                        comet,
                        group,
                        event.user.toCometUser(comet),
                    ).broadcast()
                }
            }
        }
    }
}
