package ren.natsuyuk1.comet.mirai

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.MiraiConfigManager

class MiraiWrapper : CometWrapper {
    override suspend fun createInstance(config: CometConfig, classLoader: ClassLoader): Comet {
        MiraiConfigManager.init()

        var miraiConfig = MiraiConfigManager.findMiraiConfigByID(config.id)

        if (miraiConfig == null) {
            miraiConfig = MiraiConfig(config.id).also { it.init() }
            AccountData.registerAccount(config.id, config.password, platform())
        }

        return MiraiComet(config, classLoader, miraiConfig)
    }

    override fun platform(): LoginPlatform = LoginPlatform.MIRAI
}
