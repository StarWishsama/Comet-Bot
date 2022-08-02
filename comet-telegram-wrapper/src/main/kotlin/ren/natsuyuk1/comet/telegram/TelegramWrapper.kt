package ren.natsuyuk1.comet.telegram

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper
import ren.natsuyuk1.comet.telegram.config.TelegramConfig
import ren.natsuyuk1.comet.telegram.config.findTelegramConfigByID

object TelegramWrapper : CometWrapper {
    override suspend fun createInstance(config: CometConfig): Comet {
        var telegramConfig = findTelegramConfigByID(config.data.botPassword)

        if (telegramConfig == null) {
            telegramConfig = TelegramConfig(config.data.botPassword).also { it.init() }
            AccountData.registerAccount(config.data.botId, config.data.botPassword, LoginPlatform.MIRAI)
        }

        return TelegramComet(config, telegramConfig)
    }
}
