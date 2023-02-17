package ren.natsuyuk1.comet.mirai.contact

import net.mamoe.mirai.contact.getMember
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.Friend
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.mirai.MiraiComet

internal class MiraiGroupImpl(
    override val miraiContact: net.mamoe.mirai.contact.Group,
    override val comet: MiraiComet,
) : Group, MiraiContact {
    override val id: Long = miraiContact.id

    override val name: String = miraiContact.name
    override suspend fun getOwner(): GroupMember = miraiContact.owner.toGroupMember(comet)

    override suspend fun getMembers(): List<GroupMember> = miraiContact.members.toGroupMemberList(comet)

    override suspend fun updateGroupName(groupName: String) {
        miraiContact.name = groupName
    }

    override suspend fun getBotMuteRemaining(): Int = miraiContact.botMuteRemaining

    override suspend fun getBotPermission(): GroupPermission {
        return GroupPermission.values()[miraiContact.botPermission.ordinal]
    }

    override suspend fun getGroupAvatarURL(): String = miraiContact.avatarUrl

    override suspend fun getMember(id: Long): GroupMember? = miraiContact.getMember(id)?.toGroupMember(comet)

    override suspend fun quit(): Boolean = miraiContact.quit()

    override suspend fun contains(id: Long): Boolean = miraiContact.contains(id)
}

fun net.mamoe.mirai.contact.Group.toCometGroup(comet: MiraiComet): Group = MiraiGroupImpl(this, comet)

class MiraiUserImpl(
    override val miraiContact: net.mamoe.mirai.contact.User,
    override val comet: Comet,
) : User, MiraiContact {
    override val name: String = miraiContact.nick
    override val id: Long = miraiContact.id
}

fun net.mamoe.mirai.contact.User.toCometUser(miraiComet: MiraiComet): User =
    MiraiUserImpl(this, miraiComet)

class MiraiFriend(
    override val miraiContact: net.mamoe.mirai.contact.Friend,
    override val comet: MiraiComet,
) : Friend, MiraiContact {
    override val id: Long
        get() = miraiContact.id
    override val name: String
        get() = miraiContact.nick
    override val platform: CometPlatform
        get() = CometPlatform.MIRAI
}

fun net.mamoe.mirai.contact.Friend.toCometFriend(miraiComet: MiraiComet): Friend =
    MiraiFriend(this, miraiComet)
