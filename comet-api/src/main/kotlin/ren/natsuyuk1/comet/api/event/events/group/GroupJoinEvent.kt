package ren.natsuyuk1.comet.api.event.events.group

import ren.natsuyuk1.comet.api.event.AbstractEvent
import ren.natsuyuk1.comet.api.user.GroupMember

sealed class GroupJoinEvent(
    override val member: GroupMember,
) : IGroupMemberEvent, AbstractEvent() {
    /**
     * 被他人邀请入群
     */
    data class Invite(
        override val member: GroupMember,
        val invitor: GroupMember,
    ) : GroupJoinEvent(member)

    /**
     * 通过正常渠道加入群
     */
    data class Normal(
        override val member: GroupMember,
    ) : GroupJoinEvent(member)

    /**
     * Mirai 平台独有事件, 该群为群主通过 `https://huifu.qq.com` 恢复.
     */
    data class Retrieve(
        override val member: GroupMember,
    ) : GroupJoinEvent(member)
}
