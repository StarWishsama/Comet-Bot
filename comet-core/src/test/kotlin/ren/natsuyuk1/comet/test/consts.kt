package ren.natsuyuk1.comet.test

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.cometInstances
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

private val logger = KotlinLogging.logger {}

val fakeGroups = mutableSetOf<Group>()

val fakeComet by lazy {
    object :
        Comet(LoginPlatform.TEST, CometConfig(0, "", LoginPlatform.TEST), logger, ModuleScope("fake-comet-core")) {
        override val id: Long = 0

        override fun login() {}

        override fun afterLogin() {}

        override fun close() {}

        override suspend fun getGroup(id: Long): Group? = fakeGroups.find { it.id == id }

        override suspend fun deleteMessage(source: MessageSource) = false

        override suspend fun getFriend(id: Long): User? {
            return null
        }

        override suspend fun getStranger(id: Long): User? {
            return null
        }

        override suspend fun reply(message: MessageWrapper, receipt: MessageReceipt): MessageReceipt? {
            return null
        }
    }.also { cometInstances.add(it) }
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

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        logger.debug { "Message sent to user $id: ${message.encodeToString()}" }
        return null
    }
}

fun generateFakeGroup(id: Long): Group = object : Group(id, "TestGroup") {
    override suspend fun getOwner(): GroupMember = error("dummy cannot invoke this")

    override suspend fun getMembers(): List<GroupMember> = error("dummy cannot invoke this")

    override fun updateGroupName(groupName: String) {
        error("dummy cannot invoke this")
    }

    override fun getBotMuteRemaining(): Int {
        error("dummy cannot invoke this")
    }

    override suspend fun getBotPermission(): GroupPermission {
        error("dummy cannot invoke this")
    }

    override suspend fun avatarUrl(): String = error("dummy cannot invoke this")

    override suspend fun getMember(id: Long): GroupMember? {
        error("dummy cannot invoke this")
    }

    override suspend fun quit(): Boolean {
        error("dummy cannot invoke this")
    }

    override suspend fun contains(id: Long): Boolean {
        error("dummy cannot invoke this")
    }

    override val comet: Comet = fakeComet
    override val platform: LoginPlatform = LoginPlatform.TEST

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        logger.debug { "Message sent to group $id: ${message.encodeToString()}" }
        return null
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

    override suspend fun getGroupPermission(): GroupPermission = error("dummy")

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

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        logger.debug { "FakeGroupMember($id) received: $message" }
        return null
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
