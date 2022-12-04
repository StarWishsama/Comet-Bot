package ren.natsuyuk1.comet.mirai

import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.wrapper.CometWrapper
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.MiraiConfigManager
import ren.natsuyuk1.comet.mirai.util.runWith

private val logger = KotlinLogging.logger {}

class MiraiWrapper : CometWrapper {
    override suspend fun createInstance(config: CometConfig): Comet {
        MiraiConfigManager.init()

        var loginProtocol = config.protocol

        var miraiConfig = MiraiConfigManager.findMiraiConfigByID(config.id)

        if (miraiConfig == null) {
            miraiConfig = MiraiConfig(config.id).also { it.init() }
        }

        if (loginProtocol == null) {
            logger.warn { "检测到旧版本数据库, 将使用当前 Mirai 配置文件设置的协议 (${miraiConfig.protocol}) 登录." }
            loginProtocol = miraiConfig.protocol
            transaction {
                AccountData.getAccountData(config.id, platform())?.protocol = loginProtocol
            }
        }

        if (!AccountData.hasAccount(config.id, platform())) {
            AccountData.registerAccount(config.id, config.password, platform(), loginProtocol)
        }

        return config.classLoader.runWith { MiraiComet(config, miraiConfig) }
    }

    override fun platform(): LoginPlatform = LoginPlatform.MIRAI

    override fun libInfo(): String = "Mirai $miraiVersion"
}
