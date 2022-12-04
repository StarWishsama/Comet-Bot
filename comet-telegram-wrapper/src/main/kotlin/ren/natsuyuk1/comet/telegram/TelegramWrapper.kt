package ren.natsuyuk1.comet.telegram

import org.jline.reader.LineReader
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometStartupData
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper

class TelegramWrapper : CometWrapper {
    override suspend fun createInstance(
        config: CometStartupData,
        classLoader: ClassLoader,
        reader: LineReader
    ): Comet {
        if (!AccountData.hasAccount(config.id, platform())) {
            AccountData.registerAccount(config.id, config.password, platform(), null)
        }

        return TelegramComet(config)
    }

    override fun platform(): LoginPlatform = LoginPlatform.TELEGRAM

    override fun libInfo(): String = "TGBotAPI $tgbotAPI"
}
