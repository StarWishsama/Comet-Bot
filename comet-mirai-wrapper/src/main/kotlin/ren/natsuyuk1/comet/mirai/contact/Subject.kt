package ren.natsuyuk1.comet.mirai.contact

import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import net.mamoe.mirai.contact.getMember
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.util.toMessageChain
import ren.natsuyuk1.comet.utils.message.MessageWrapper

abstract class MiraiGroup(
    override val id: Long,
    override var name: String
) : Group(id, name) {
    override val platform: LoginPlatform = LoginPlatform.MIRAI
}

internal class MiraiGroupImpl(
    private val group: net.mamoe.mirai.contact.Group,
    override val comet: MiraiComet
) : MiraiGroup(
    group.id,
    group.name
) {
    override val owner: GroupMember
        get() = group.owner.toGroupMember(comet)

    override val members: List<GroupMember>
        get() = group.members.toGroupMemberList(comet)

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

    override fun sendMessage(message: MessageWrapper) {
        comet.scope.launch {
            val event = MessagePreSendEvent(
                comet,
                this@MiraiGroupImpl,
                message,
                Clock.System.now().epochSeconds
            ).also { it.broadcast() }

            if (!event.isCancelled) {
                group.sendMessage(message.toMessageChain(group))
            }
        }
    }
}

fun net.mamoe.mirai.contact.Group.toCometGroup(comet: MiraiComet): Group = MiraiGroupImpl(this, comet)

abstract class MiraiUser : User() {
    override val platform: LoginPlatform = LoginPlatform.MIRAI
}

fun net.mamoe.mirai.contact.User.toCometUser(miraiComet: MiraiComet): User {
    val miraiUser = this@toCometUser

    class MiraiUserImpl(
        override val comet: Comet = miraiComet,
        override val name: String = miraiUser.nick,
        override val id: Long = miraiUser.id
    ) : MiraiUser() {
        override fun sendMessage(message: MessageWrapper) {
            comet.scope.launch {
                val event = MessagePreSendEvent(
                    comet,
                    this@MiraiUserImpl,
                    message,
                    Clock.System.now().epochSeconds
                ).also { it.broadcast() }

                if (!event.isCancelled) {
                    miraiUser.sendMessage(message.toMessageChain(miraiUser))
                }
            }
        }
    }

    return MiraiUserImpl()
}
