package ren.natsuyuk1.comet.api.test

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.message.MessageWrapper

private val logger = mu.KotlinLogging.logger {}

val fakeComet = object : Comet(CometConfig, logger, ModuleScope("TestInstance")) {
    override fun login() {}

    override fun afterLogin() {}

    override fun close() {}
}

val fakeSender = object : PlatformCommandSender() {
    override val comet: Comet
        get() = fakeComet
    override val id: Long = 0
    override val name: String = "Dummy"
    override var card: String = "Dummy"

    override fun sendMessage(message: MessageWrapper) {
        logger.info { message.parseToString() }
    }
}
