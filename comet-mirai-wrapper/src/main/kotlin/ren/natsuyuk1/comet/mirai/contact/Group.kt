package ren.natsuyuk1.comet.mirai.contact

import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.NormalMember
import ren.natsuyuk1.comet.api.event.broadcast
import ren.natsuyuk1.comet.api.event.events.comet.MessagePreSendEvent
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.util.toMessageChain
import ren.natsuyuk1.comet.utils.message.MessageWrapper

fun Member.toGroupMember(comet: MiraiComet): GroupMember {
    return when (this) {
        is NormalMember -> this.toGroupMember(comet)
        is AnonymousMember -> this.toGroupMember(comet)
        else -> error("Unsupported mirai side member (${this::class.simpleName})")
    }
}

internal abstract class MiraiGroupMember : GroupMember() {
    override val platform: LoginPlatform = LoginPlatform.MIRAI
}

internal class MiraiGroupMemberImpl(
    private val contact: NormalMember,
    override val comet: MiraiComet
) : MiraiGroupMember() {
    override val group: Group
        get() = contact.group.toCometGroup(comet)

    override val id: Long
        get() = contact.id

    override val joinTimestamp: Int
        get() = contact.joinTimestamp

    override val lastActiveTimestamp: Int
        get() = contact.lastSpeakTimestamp

    override val remainMuteTime: Int
        get() = contact.muteTimeRemaining

    override suspend fun mute(seconds: Int) {
        contact.mute(seconds)
    }

    override suspend fun unmute() = contact.unmute()

    override suspend fun kick(reason: String, block: Boolean) {
        contact.kick(reason, block)
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        contact.modifyAdmin(operation)
    }

    override fun sendMessage(message: MessageWrapper) {
        comet.scope.launch {
            val event = MessagePreSendEvent(
                comet,
                this@MiraiGroupMemberImpl,
                message,
                Clock.System.now().epochSeconds
            ).also { it.broadcast() }

            if (!event.isCancelled)
                contact.sendMessage(message.toMessageChain(contact))
        }
    }

    override val name: String
        get() = contact.nick

    override var card: String
        get() = contact.nameCard
        set(value) {
            contact.nameCard = value
        }
}

fun NormalMember.toGroupMember(comet: MiraiComet): GroupMember = MiraiGroupMemberImpl(this, comet)

internal class MiraiAnonymousMemberImpl(
    private val contact: AnonymousMember,
    override val comet: MiraiComet,
) : ren.natsuyuk1.comet.api.user.AnonymousMember() {
    override val group: Group
        get() = contact.group.toCometGroup(comet)

    override val platform: LoginPlatform
        get() = LoginPlatform.MIRAI

    override val anonymousId: String
        get() = contact.anonymousId

    override val id: Long
        get() = contact.id

    /**
     * 匿名成员无此变量, 默认返回 -1
     */
    override val joinTimestamp: Int
        get() = -1

    /**
     * 匿名成员无此变量, 默认返回 -1
     */
    override val lastActiveTimestamp: Int
        get() = -1

    /**
     * 匿名成员无此变量, 默认返回 -1
     */
    override val remainMuteTime: Int
        get() = -1

    override suspend fun mute(seconds: Int) {
        contact.mute(seconds)
    }

    override suspend fun unmute() = contact.mute(0)

    override suspend fun kick(reason: String, block: Boolean) {
        error("AnonymousMember cannot be kicked")
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        error("AnonymousMember cannot be promoted")
    }

    override fun sendMessage(message: MessageWrapper) {
        error("Cannot send message to AnonymousMember")
    }

    override val name: String
        get() = contact.nick

    override var card: String
        get() = contact.nameCard
        set(_) {
            error("Cannot modify namecard of AnonymousMember")
        }
}

fun AnonymousMember.toGroupMember(comet: MiraiComet): GroupMember = MiraiAnonymousMemberImpl(this, comet)

fun ContactList<NormalMember>.toGroupMemberList(comet: MiraiComet): List<GroupMember> {
    val converted = mutableListOf<GroupMember>()
    for (normalMember in this) {
        converted.add(normalMember.toGroupMember(comet))
    }

    return converted
}
