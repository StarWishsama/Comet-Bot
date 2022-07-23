package ren.natsuyuk1.comet.test

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.message.MessageWrapper

private val logger = KotlinLogging.logger {}

val fakeComet = object : Comet(CometConfig, logger, ModuleScope("fake-comet-core")) {
    override fun login() {}

    override fun afterLogin() {}

    override fun close() {}

    override fun getGroup(id: Long): Group? = null

}

fun generateFakeSender(id: Long): PlatformCommandSender = object : PlatformCommandSender() {
    override val comet: Comet
        get() = fakeComet
    override val id: Long
        get() = id
    override val name: String
        get() = "test"
    override var card: String
        get() = "test"
        set(_) {}
    override val platformName: String
        get() = "test"

    override fun sendMessage(message: MessageWrapper) {
        logger.info { "Message sent to user ${id}: ${message.parseToString()}" }
    }
}

fun Any.print() = println(this)

fun isCI() = System.getenv("CI") != null
