package ren.natsuyuk1.comet.kook

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.message.MessageReceipt
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope

private val logger = mu.KotlinLogging.logger("Comet-KOOK")

class KookComet(
    /**
     * 一个 Comet 实例的 [CometConfig]
     */
    config: CometConfig,
    override val id: Long,
) : Comet(
    LoginPlatform.KOOK, config, logger, ModuleScope("kook (${config.id}")
) {
    override fun login() {
        TODO("Not yet implemented")
    }

    override fun afterLogin() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override suspend fun reply(message: MessageWrapper, receipt: MessageReceipt): MessageReceipt? {
        TODO("Not yet implemented")
    }

    override suspend fun getGroup(id: Long): Group? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(source: MessageSource): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getFriend(id: Long): User? {
        TODO("Not yet implemented")
    }

    override suspend fun getStranger(id: Long): User? {
        TODO("Not yet implemented")
    }
}
