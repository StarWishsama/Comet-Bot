package ren.natsuyuk1.comet.test

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.message.MessageWrapper

private val logger = KotlinLogging.logger {}

val fakeComet = object : Comet(CometConfig(0, "", LoginPlatform.TEST), logger, ModuleScope("fake-comet-core")) {
    override val id: Long = 0

    override fun login() {}

    override fun afterLogin() {}

    override fun close() {}

    override suspend fun getGroup(id: Long): Group? = null
}

fun generateFakeSender(id: Long): User = object : User() {
    override val comet: Comet
        get() = fakeComet
    override val id: Long
        get() = id
    override val name: String
        get() = "testuser"
    override val platform: LoginPlatform
        get() = LoginPlatform.TEST

    override fun sendMessage(message: MessageWrapper) {
        logger.debug { "Message sent to user $id: ${message.parseToString()}" }
    }
}

fun generateFakeGroup(id: Long): Group = object : Group(id, "TestGroup") {
    override val owner: GroupMember
        get() = error("dummy cannot invoke this")
    override val members: List<GroupMember>
        get() = error("dummy cannot invoke this")

    override fun updateGroupName(groupName: String) {
        error("dummy cannot invoke this")
    }

    override fun getBotMuteRemaining(): Int {
        error("dummy cannot invoke this")
    }

    override fun getBotPermission(): GroupPermission {
        error("dummy cannot invoke this")
    }

    override val avatarUrl: String
        get() = error("dummy cannot invoke this")

    override fun getMember(id: Long): GroupMember? {
        error("dummy cannot invoke this")
    }

    override suspend fun quit(): Boolean {
        error("dummy cannot invoke this")
    }

    override fun contains(id: Long): Boolean {
        error("dummy cannot invoke this")
    }

    override val comet: Comet = fakeComet
    override val platform: LoginPlatform = LoginPlatform.TEST

    override fun sendMessage(message: MessageWrapper) {
        logger.debug { "Message sent to group $id: ${message.parseToString()}" }
    }
}

fun generateFakeGroupMember(id: Long, group: Group) = object : GroupMember() {
    override val group: Group
        get() = group
    override val id: Long
        get() = id
    override val joinTimestamp: Int
        get() = error("dummy")
    override val lastActiveTimestamp: Int
        get() = error("dummy")
    override val remainMuteTime: Int
        get() = error("dummy")
    override val card: String
        get() = "Test"
    override val groupPermission: GroupPermission
        get() = error("dummy")

    override suspend fun mute(seconds: Int) {
        error("dummy")
    }

    override suspend fun unmute() {
        error("dummy")
    }

    override suspend fun kick(reason: String, block: Boolean) {
        error("dummy")
    }

    override suspend fun operateAdminPermission(operation: Boolean) {
        error("dummy")
    }

    override fun sendMessage(message: MessageWrapper) {
        logger.debug { "FakeGroupMember($id) received: $message" }
    }

    override val comet: Comet
        get() = fakeComet
    override val name: String
        get() = "Test"
    override val platform: LoginPlatform
        get() = LoginPlatform.TEST

}

fun Any.print() = println(this)

fun isCI() = System.getenv("CI") != null
