package ren.natsuyuk1.comet.cli.util

import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.database.AccountDataTable
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.cli.CometTerminal
import ren.natsuyuk1.comet.cli.CometTerminalCommand
import ren.natsuyuk1.comet.cli.WrapperLoader

private val logger = KotlinLogging.logger {}

internal suspend fun login(id: Long, password: String, platform: LoginPlatform) {
    logger.info { "正在尝试登录账号 $id 于 ${platform.name} 平台" }

    when (platform) {
        LoginPlatform.MIRAI -> {
            val miraiService = WrapperLoader.getService(platform) ?: error("未安装 Mirai Wrapper")

            val miraiComet = miraiService.createInstance(CometConfig(id, password, platform))

            CometTerminal.instance.push(miraiComet)
            miraiComet.init(CometTerminalCommand.scope.coroutineContext)
            miraiComet.login()
            miraiComet.afterLogin()
        }

        LoginPlatform.TELEGRAM -> {
            val telegramService = WrapperLoader.getService(platform) ?: error("未安装 Telegram Wrapper")

            val telegramComet = telegramService.createInstance(CometConfig(id, password, platform))

            CometTerminal.instance.push(telegramComet)
            telegramComet.init(CometTerminalCommand.scope.coroutineContext)
            telegramComet.login()
            telegramComet.afterLogin()
        }

        else -> {}
    }
}

internal fun logout(id: Long, platform: LoginPlatform) {
    logger.info { "正在尝试注销账号 $id 于 ${platform.name} 平台" }

    if (!AccountData.hasAccount(id, platform)) {
        logger.info { "注销账号失败: 找不到对应账号" }
    } else {
        transaction {
            AccountDataTable.deleteWhere {
                AccountDataTable.id eq id and (AccountDataTable.platform eq platform)
            }
        }

        CometTerminal.instance.find { it.id == id }?.close()
        CometTerminal.instance.removeIf { it.id == id }

        logger.info { "注销账号成功" }
    }
}
