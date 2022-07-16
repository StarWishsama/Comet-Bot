package ren.natsuyuk1.comet.cli.util

import mu.KotlinLogging
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.cli.CometTerminal
import ren.natsuyuk1.comet.cli.CometTerminalCommand
import ren.natsuyuk1.comet.cli.storage.AccountData
import ren.natsuyuk1.comet.cli.storage.LoginPlatform
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.config.MiraiConfig
import ren.natsuyuk1.comet.mirai.config.findMiraiConfigByID
import ren.natsuyuk1.comet.telegram.TelegramComet
import ren.natsuyuk1.comet.telegram.config.TelegramConfig
import ren.natsuyuk1.comet.telegram.config.findTelegramConfigByID

private val logger = KotlinLogging.logger {}

internal suspend fun login(id: String, password: String, platform: LoginPlatform) {
    logger.info { "正在尝试登录账号 $id 于 ${platform.name} 平台" }

    when (platform) {
        LoginPlatform.QQ -> {
            var instanceConfig = findMiraiConfigByID(id.toLong())

            if (instanceConfig == null) {
                instanceConfig = MiraiConfig(id.toLong(), password).also { it.init() }
                AccountData.registerAccount(id, password, platform)
            }

            val miraiComet = MiraiComet(CometConfig, instanceConfig)

            CometTerminal.instance.push(miraiComet)
            miraiComet.init(CometTerminalCommand.scope.coroutineContext)
            miraiComet.login()
            miraiComet.afterLogin()
        }
        LoginPlatform.TELEGRAM -> {
            var instanceConfig = findTelegramConfigByID(id)

            if (instanceConfig == null) {
                instanceConfig = TelegramConfig(id).also { it.init() }
                AccountData.registerAccount(id, "", platform)
            }

            val telegramComet = TelegramComet(CometConfig, instanceConfig)
            CometTerminal.instance.push(telegramComet)
            telegramComet.init(CometTerminalCommand.scope.coroutineContext)
            telegramComet.login()
            telegramComet.afterLogin()
        }
    }
}
