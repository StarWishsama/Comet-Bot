package ren.natsuyuk1.comet.telegram

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper

class TelegramWrapper : CometWrapper {
    override suspend fun createInstance(config: CometConfig, classLoader: ClassLoader): Comet {
        AccountData.registerAccount(config.id, config.password, platform())

        return TelegramComet(config)
    }

    override fun platform(): LoginPlatform = LoginPlatform.TELEGRAM
}
