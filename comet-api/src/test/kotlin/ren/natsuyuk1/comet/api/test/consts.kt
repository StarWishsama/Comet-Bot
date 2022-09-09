package ren.natsuyuk1.comet.api.test

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

private val logger = mu.KotlinLogging.logger {}

val fakeComet = object : Comet(LoginPlatform.TEST, CometConfig(0, "", LoginPlatform.TEST), logger, ModuleScope("TestInstance")) {
    override val id: Long = 0

    override fun login() {}

    override fun afterLogin() {}

    override fun close() {}

    override suspend fun getGroup(id: Long): Group? = null
}

val fakeSender = object : PlatformCommandSender() {
    override val comet: Comet
        get() = fakeComet
    override val id: Long = 0
    override val name: String = "Dummy"
    override val platform: LoginPlatform = LoginPlatform.TEST

    override suspend fun sendMessage(message: MessageWrapper) {
        logger.info { message.parseToString() }
    }
}
