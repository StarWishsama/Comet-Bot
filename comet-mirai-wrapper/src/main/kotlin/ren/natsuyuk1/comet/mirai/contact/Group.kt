package ren.natsuyuk1.comet.mirai.contact

import kotlinx.datetime.Instant
import net.mamoe.mirai.contact.*
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.mirai.MiraiComet
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Member.toGroupMember(comet: MiraiComet): GroupMember {
    return when (this) {
        is NormalMember -> this.toGroupMember(comet)
        is AnonymousMember -> this.toGroupMember(comet)
        else -> error("Unsupported mirai side member (${this::class.simpleName})")
    }
}

internal class MiraiGroupMemberImpl(
    override val miraiContact: NormalMember,
    override val comet: MiraiComet
) : MiraiContact, GroupMember {
    override val group: Group
        get() = miraiContact.group.toCometGroup(comet)

    override val id: Long
        get() = miraiContact.id

    override suspend fun getGroupPermission(): GroupPermission = miraiContact.permission.toGroupPermission()

    override suspend fun mute(seconds: Int) {
        miraiContact.mute(seconds)
    }

    override suspend fun getJoinTime(): Instant =
        Instant.fromEpochSeconds(miraiContact.joinTimestamp.toLong())

    override suspend fun getLastActiveTime(): Instant =
        Instant.fromEpochSeconds(miraiContact.lastSpeakTimestamp.toLong())

    override suspend fun getRemainMuteTime(): Duration =
        miraiContact.muteTimeRemaining.seconds

    override suspend fun unmute() = miraiContact.unmute()

    override suspend fun kick(reason: String, block: Boolean) {
        miraiContact.kick(reason, block)
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        miraiContact.modifyAdmin(operation)
    }

    override val name: String
        get() = miraiContact.nick

    override var card: String
        get() = miraiContact.nameCard
        set(value) {
            miraiContact.nameCard = value
        }
}

fun MemberPermission.toGroupPermission(): GroupPermission = GroupPermission.values()[ordinal]

fun NormalMember.toGroupMember(comet: MiraiComet): GroupMember = MiraiGroupMemberImpl(this, comet)

internal class MiraiAnonymousMemberImpl(
    override val miraiContact: AnonymousMember,
    override val comet: MiraiComet
) : ren.natsuyuk1.comet.api.user.AnonymousMember, MiraiContact {
    override val group: Group
        get() = miraiContact.group.toCometGroup(comet)

    override val platform: LoginPlatform
        get() = LoginPlatform.MIRAI

    override val anonymousId: String
        get() = miraiContact.anonymousId

    override val id: Long
        get() = miraiContact.id

    override suspend fun getGroupPermission(): GroupPermission = GroupPermission.MEMBER

    override suspend fun mute(seconds: Int) {
        miraiContact.mute(seconds)
    }

    override suspend fun unmute() = miraiContact.mute(0)

    override suspend fun kick(reason: String, block: Boolean) {
        error("AnonymousMember cannot be kicked")
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        error("AnonymousMember cannot be promoted")
    }

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        error("Cannot send message to AnonymousMember")
    }

    override val name: String
        get() = miraiContact.nick

    override var card: String
        get() = miraiContact.nameCard
        set(_) {
            error("Cannot modify namecard of AnonymousMember")
        }

    override suspend fun getJoinTime(): Instant =
        error("AnonymousMember doesn't have join time")

    override suspend fun getLastActiveTime(): Instant =
        error("AnonymousMember doesn't have join time")

    override suspend fun getRemainMuteTime(): Duration =
        error("AnonymousMember doesn't have remaining mute time")
}

fun AnonymousMember.toGroupMember(comet: MiraiComet): GroupMember = MiraiAnonymousMemberImpl(this, comet)

fun ContactList<NormalMember>.toGroupMemberList(comet: MiraiComet): List<GroupMember> {
    val converted = mutableListOf<GroupMember>()
    for (normalMember in this) {
        converted.add(normalMember.toGroupMember(comet))
    }

    return converted
}
