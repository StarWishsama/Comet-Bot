package ren.natsuyuk1.comet.mirai

import org.jline.reader.LineReader
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol
import ren.natsuyuk1.comet.api.wrapper.CometWrapper
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.MiraiConfigManager
import ren.natsuyuk1.comet.mirai.util.runWith

class MiraiWrapper : CometWrapper {
    override suspend fun createInstance(
        config: CometConfig,
        protocol: String,
        classLoader: ClassLoader,
        reader: LineReader
    ): Comet {
        MiraiConfigManager.init()

        val loginProtocol: MiraiLoginProtocol

        try {
            loginProtocol = MiraiLoginProtocol.valueOf(protocol)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("输入的 Mirai 协议名称有误, 请重新输入!")
        }

        var miraiConfig = MiraiConfigManager.findMiraiConfigByID(config.id)

        if (miraiConfig == null) {
            miraiConfig = MiraiConfig(config.id).also { it.init() }
        }

        miraiConfig.protocol = loginProtocol

        AccountData.registerAccount(config.id, config.password, platform())

        return classLoader.runWith { MiraiComet(config, classLoader, miraiConfig, reader) }
    }

    override fun platform(): LoginPlatform = LoginPlatform.MIRAI
}
