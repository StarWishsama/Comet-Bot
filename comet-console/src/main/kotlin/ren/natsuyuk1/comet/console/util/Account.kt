package ren.natsuyuk1.comet.console.util

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
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
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol
import ren.natsuyuk1.comet.console.CometTerminalCommand
import ren.natsuyuk1.comet.console.wrapper.WrapperLoader

private val logger = KotlinLogging.logger {}

val loginStatus = atomic(false)

fun createCometConfig(id: Long, password: String, platform: LoginPlatform): CometConfig {
    return CometConfig(id, password, platform)
}

internal suspend fun login(id: Long, password: String, platform: LoginPlatform, protocol: MiraiLoginProtocol?) {
    loginStatus.update { true }
    val service = WrapperLoader.getService(platform)
        ?: error("未安装 ${platform.name} Wrapper, 请下载 ${platform.name} Wrapper 并放置在 ./modules 下")
    logger.info { "正在尝试登录账号 $id 于 ${platform.name} 平台" }

    try {
        when (platform) {
            LoginPlatform.MIRAI -> {
                if (protocol == null) {
                    logger.warn { "未指定 Mirai 登录协议, 将默认设置为 ANDROID_PHONE (安卓手机协议)." }
                }

                val miraiComet =
                    service.createInstance(
                        createCometConfig(id, password, platform),
                        protocol,
                        WrapperLoader.wrapperClassLoader,
                        Console.newLineReader("mirai-comet")
                    )

                cometInstances.push(miraiComet)
                miraiComet.init(CometTerminalCommand.scope.coroutineContext)

                try {
                    miraiComet.login()
                    miraiComet.afterLogin()
                } catch (e: RuntimeException) {
                    logger.warn(e) { "Mirai $id 登录失败, 请尝试重新登录" }
                    miraiComet.close()
                    cometInstances.remove(miraiComet)
                }
            }

            LoginPlatform.TELEGRAM -> {
                val telegramComet =
                    service.createInstance(
                        createCometConfig(id, password, platform),
                        protocol,
                        WrapperLoader.wrapperClassLoader,
                        Console.newLineReader("telegram-comet")
                    )

                cometInstances.push(telegramComet)
                telegramComet.init(CometTerminalCommand.scope.coroutineContext)

                try {
                    telegramComet.login()
                    telegramComet.afterLogin()
                } catch (e: Exception) {
                    logger.warn(e) { "Telegram $id 登录失败, 请尝试重新登录" }
                    telegramComet.close()
                    cometInstances.remove(telegramComet)
                }
            }

            else -> {}
        }
    } finally {
        loginStatus.update { false }
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
