package ren.natsuyuk1.comet.cli.util

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.cli.CometTerminal
import ren.natsuyuk1.comet.cli.storage.AccountData
import ren.natsuyuk1.comet.cli.storage.LoginPlatform
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.findMiraiConfigByID

private val logger = KotlinLogging.logger {}

suspend fun login(id: Long, password: String, platform: LoginPlatform) {
    logger.info { "正在尝试登录账号 $id 于 ${platform.name} 平台" }

    when (platform) {
        LoginPlatform.QQ -> {
            var instanceConfig = findMiraiConfigByID(id)

            if (instanceConfig == null) {
                instanceConfig = MiraiConfig(id, password).also { it.init() }
                AccountData.registerAccount(id, password, platform)
            }

            val miraiComet = MiraiComet(CometConfig, instanceConfig)

            CometTerminal.instance.push(miraiComet)
            miraiComet.login()
            miraiComet.afterLogin()
        }
        LoginPlatform.TELEGRAM -> {
            TODO("Not implemented")

            // AccountData.registerAccount(id, password, LoginPlatform.TELEGRAM)
        }
    }
}
