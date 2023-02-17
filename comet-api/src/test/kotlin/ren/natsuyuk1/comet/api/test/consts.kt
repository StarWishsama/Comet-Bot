package ren.natsuyuk1.comet.api.test

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

private val logger = mu.KotlinLogging.logger {}

val fakeComet = object : Comet(
    platform = CometPlatform.TEST,
    config = CometConfig(0, "", CometPlatform.TEST),
    logger = logger,
    scope = ModuleScope("TestInstance"),
) {
    override val id: Long = 0

    override fun login() {}

    override fun afterLogin() {}

    override fun close() {}

    override suspend fun getGroup(id: Long): Group? = null

    override suspend fun deleteMessage(source: MessageSource): Boolean = false

    override suspend fun getFriend(id: Long): User? {
        return null
    }

    override suspend fun getStranger(id: Long): User? {
        return null
    }

    override suspend fun reply(message: MessageWrapper, receipt: MessageReceipt): MessageReceipt? {
        return null
    }
}

val fakeSender = object : PlatformCommandSender {
    override val comet: Comet
        get() = fakeComet
    override val id: Long = 0
    override val name: String = "Dummy"
    override val platform: CometPlatform = CometPlatform.TEST

    override suspend fun sendMessage(message: MessageWrapper): MessageReceipt? {
        logger.info { message.encodeToString() }
        return null
    }
}
