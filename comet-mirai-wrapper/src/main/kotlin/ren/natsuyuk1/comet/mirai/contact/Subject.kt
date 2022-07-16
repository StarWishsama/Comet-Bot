package ren.natsuyuk1.comet.mirai.contact

import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import net.mamoe.mirai.contact.getMember
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.impl.comet.MessageSendEvent
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.util.toMessageChain
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class MiraiGroup(
    override val id: Long,
    override var name: String,
) : Group(id, name) {
    override val platformName: String = "mirai"
}

fun net.mamoe.mirai.contact.Group.toCometGroup(comet: MiraiComet): Group {
    val group = this@toCometGroup

    class MiraiGroupImpl : MiraiGroup(
        group.id,
        group.name
    ) {
        override val owner: GroupMember = group.owner.toGroupMember(comet)

        override val members: List<GroupMember> = group.members.toGroupMemberList(comet)

        override fun updateGroupName(groupName: String) {
            group.name = groupName
        }

        override fun getBotMuteRemaining(): Int = group.botMuteRemaining

        override fun getBotPermission(): GroupPermission {
            return GroupPermission.valueOf(group.botPermission.name)
        }

        override val avatarUrl: String = group.avatarUrl

        override fun getMember(id: Long): GroupMember? = group.getMember(id)?.toGroupMember(comet)

        override suspend fun quit(): Boolean = group.quit()

        override fun contains(id: Long): Boolean = group.contains(id)
        override val comet: Comet
            get() = comet

        // Group doesn't have card
        override var card: String = ""

        override fun sendMessage(message: MessageWrapper) {
            comet.scope.launch {
                val event = MessageSendEvent(
                    comet,
                    this@MiraiGroupImpl,
                    message,
                    Clock.System.now().epochSeconds
                ).also { it.broadcast() }

                if (!event.isCancelled)
                    group.sendMessage(message.toMessageChain(group))
            }
        }
    }

    return MiraiGroupImpl()
}
