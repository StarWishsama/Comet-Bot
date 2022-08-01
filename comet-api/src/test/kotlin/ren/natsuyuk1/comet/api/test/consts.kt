package ren.natsuyuk1.comet.api.test

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.message.MessageWrapper

private val logger = mu.KotlinLogging.logger {}

val fakeComet = object : Comet(CometConfig, logger, ModuleScope("TestInstance")) {
    override val id: String
        get() = ""

    override fun login() {}

    override fun afterLogin() {}

    override fun close() {}

    override fun getGroup(id: Long): Group? = null
}

val fakeSender = object : PlatformCommandSender() {
    override val comet: Comet
        get() = fakeComet
    override val id: Long = 0
    override val name: String = "Dummy"
    override var card: String = "Dummy"
    override val platformName: String = "mirai"

    override fun sendMessage(message: MessageWrapper) {
        logger.info { message.parseToString() }
    }
}
