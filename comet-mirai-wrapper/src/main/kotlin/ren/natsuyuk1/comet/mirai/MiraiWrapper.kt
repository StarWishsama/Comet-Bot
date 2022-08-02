package ren.natsuyuk1.comet.mirai

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.findMiraiConfigByID

class MiraiWrapper : CometWrapper {
    override suspend fun createInstance(config: CometConfig): Comet {
        var miraiConfig = findMiraiConfigByID(config.data.botId)

        if (miraiConfig == null) {
            miraiConfig = MiraiConfig(config.data.botId, config.data.botPassword).also { it.init() }
            AccountData.registerAccount(config.data.botId, config.data.botPassword, platform())
        }

        return MiraiComet(config, miraiConfig)
    }

    override fun platform(): LoginPlatform = LoginPlatform.MIRAI
}
