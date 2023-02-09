package ren.natsuyuk1.comet.telegram

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper

class TelegramWrapper : CometWrapper {
    override suspend fun createInstance(
        config: CometConfig
    ): Comet {
        if (!AccountData.hasAccount(config.id, platform())) {
            AccountData.registerAccount(config.id, config.password, platform(), null)
        }

        return TelegramComet(config)
    }

    override fun platform(): CometPlatform = CometPlatform.TELEGRAM

    override fun libInfo(): String = "TGBotAPI $tgbotAPI"
}
