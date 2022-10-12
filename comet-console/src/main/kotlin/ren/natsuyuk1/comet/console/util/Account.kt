package ren.natsuyuk1.comet.console.util

import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.cometInstances
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountData
import ren.natsuyuk1.comet.api.database.AccountDataTable
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.console.CometTerminalCommand
import ren.natsuyuk1.comet.console.wrapper.WrapperLoader

private val logger = KotlinLogging.logger {}

fun createCometConfig(id: Long, password: String, platform: LoginPlatform): CometConfig {
    return CometConfig(id, password, platform)
}

internal suspend fun login(id: Long, password: String, platform: LoginPlatform) {
    logger.info { "正在尝试登录账号 $id 于 ${platform.name} 平台" }

    when (platform) {
        LoginPlatform.MIRAI -> {
            val miraiService = WrapperLoader.getService(platform)
                ?: error("未安装 Mirai Wrapper, 请下载 Mirai Wrapper 并放置在 ./modules 下")

            val miraiComet =
                miraiService.createInstance(
                    createCometConfig(id, password, platform),
                    WrapperLoader.wrapperClassLoader,
                    ConsoleInputReceiver
                )

            cometInstances.push(miraiComet)
            miraiComet.init(CometTerminalCommand.scope.coroutineContext)

            try {
                miraiComet.login()
                miraiComet.afterLogin()
            } catch (e: RuntimeException) {
                logger.warn(e) { "Mirai $id 登录失败, 请尝试重新登录" }
                cometInstances.removeIf { it.id == id }
            }
        }

        LoginPlatform.TELEGRAM -> {
            val telegramService = WrapperLoader.getService(platform)
                ?: error("未安装 Telegram Wrapper, 请下载 Telegram Wrapper 并放置在 ./modules 下")

            val telegramComet =
                telegramService.createInstance(
                    createCometConfig(id, password, platform),
                    WrapperLoader.wrapperClassLoader,
                    ConsoleInputReceiver
                )

            cometInstances.push(telegramComet)
            telegramComet.init(CometTerminalCommand.scope.coroutineContext)

            try {
                telegramComet.login()
                telegramComet.afterLogin()
            } catch (e: Exception) {
                logger.warn(e) { "Telegram $id 登录失败, 请尝试重新登录" }
                cometInstances.removeIf { it.id == id }
            }
        }

        else -> {}
    }
}

internal fun logout(id: Long, platform: LoginPlatform) {
    logger.info { "正在尝试注销账号 $id 于 ${platform.name} 平台" }

    if (!AccountData.hasAccount(id, platform)) {
        logger.info { "注销账号失败: 找不到对应账号" }
    } else {
        cometInstances.find { it.id == id && it.platform == platform }?.close()
        cometInstances.removeIf { it.id == id && it.platform == platform }

        transaction {
            AccountDataTable.deleteWhere {
                AccountDataTable.id eq id and
                    (AccountDataTable.platform eq platform)
            }
        }

        logger.info { "注销账号成功" }
    }
}
