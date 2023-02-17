package ren.natsuyuk1.comet.api.database

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.cometInstances
import ren.natsuyuk1.comet.api.cometScope
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.console.Console
import ren.natsuyuk1.comet.api.platform.CometPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol
import ren.natsuyuk1.comet.api.status.AccountOperationResult
import ren.natsuyuk1.comet.api.status.AccountOperationStatus
import ren.natsuyuk1.comet.api.wrapper.WrapperLoader

private val logger = KotlinLogging.logger {}

val loginStatus = atomic(false)

object AccountDataTable : IdTable<Long>("account_data") {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    val password: Column<String> = text("password")
    val platform = enumeration<CometPlatform>("platform")
    val protocol = enumeration<MiraiLoginProtocol>("protocol").nullable().default(null)
}

class AccountData(id: EntityID<Long>) : Entity<Long>(id) {
    var password by AccountDataTable.password
    var platform by AccountDataTable.platform
    var protocol by AccountDataTable.protocol

    companion object : EntityClass<Long, AccountData>(AccountDataTable) {
        fun hasAccount(id: Long, platform: CometPlatform): Boolean = transaction {
            !find {
                AccountDataTable.id eq id and (AccountDataTable.platform eq platform)
            }.empty()
        }

        fun registerAccount(
            id: Long,
            password: String,
            platform: CometPlatform,
            protocol: MiraiLoginProtocol?,
        ): AccountData {
            val target = transaction { findById(id) }

            if (hasAccount(id, platform) && target != null) {
                return target
            }

            logger.debug { "Creating comet bot account ($id) in $platform platform" }

            return transaction {
                AccountData.new(id) {
                    this.password = password
                    this.platform = platform
                    this.protocol = protocol
                }
            }
        }

        fun getAccountData(id: Long, platform: CometPlatform): AccountData? =
            transaction {
                find {
                    AccountDataTable.id eq id and (AccountDataTable.platform eq platform)
                }.firstOrNull()
            }

        suspend fun login(
            id: Long,
            password: String,
            platform: CometPlatform,
            protocol: MiraiLoginProtocol? = null,
        ): AccountOperationResult {
            if (cometInstances.any { it.id == id && it.platform == platform }) {
                return AccountOperationResult(
                    AccountOperationStatus.ALREADY_LOGON,
                    "Comet 终端已登录过相同账号!",
                ).also {
                    logger.warn { it.message }
                }
            }

            val service = WrapperLoader.getService(platform)
                ?: return AccountOperationResult(
                    AccountOperationStatus.NO_WRAPPER,
                    "未安装 ${platform.name} Wrapper, 请下载 ${platform.name} Wrapper 并放置在 ./modules 下",
                ).also {
                    logger.warn { it.message }
                }

            logger.info { "正在尝试登录账号 $id 于 ${platform.name} 平台" }

            loginStatus.update { true }

            try {
                return when (platform) {
                    CometPlatform.MIRAI -> {
                        if (protocol == null) {
                            logger.warn { "未指定 Mirai 登录协议, 将默认设置为 ANDROID_PAD (安卓手机协议). 如先前已有配置将会跟随先前配置." }
                        }

                        val miraiComet =
                            service.createInstance(
                                CometConfig(
                                    id,
                                    password,
                                    platform,
                                    protocol,
                                    WrapperLoader.wrapperClassLoader,
                                    Console.newLineReader("mirai-comet"),
                                ),
                            )

                        cometInstances.push(miraiComet)
                        miraiComet.init(cometScope.coroutineContext)

                        return try {
                            miraiComet.login()
                            miraiComet.afterLogin()
                            AccountOperationResult(AccountOperationStatus.OK, "Mirai $id 登录成功")
                        } catch (e: RuntimeException) {
                            logger.warn(e) { "Mirai $id 登录失败, 请尝试重新登录" }
                            miraiComet.close()
                            cometInstances.remove(miraiComet)
                            AccountOperationResult(
                                AccountOperationStatus.LOGIN_FAILED,
                                """
                                Mirai $id 登录失败, 请尝试重新登录
                                Mirai 渠道无法正常通过 Web API 登录,
                                请在本地登录后复制相关文件登录.    
                                """.trimIndent(),
                            )
                        }
                    }

                    CometPlatform.TELEGRAM -> {
                        val telegramComet =
                            service.createInstance(
                                CometConfig(
                                    id,
                                    password,
                                    platform,
                                    protocol,
                                    WrapperLoader.wrapperClassLoader,
                                    Console.newLineReader("telegram-comet"),
                                ),
                            )

                        cometInstances.push(telegramComet)
                        telegramComet.init(cometScope.coroutineContext)

                        try {
                            telegramComet.login()
                            telegramComet.afterLogin()
                            AccountOperationResult(AccountOperationStatus.OK, "Telegram $id 登录成功")
                        } catch (e: Exception) {
                            logger.warn(e) { "Telegram $id 登录失败, 请尝试重新登录" }
                            telegramComet.close()
                            cometInstances.remove(telegramComet)
                            AccountOperationResult(
                                AccountOperationStatus.LOGIN_FAILED,
                                "Telegram $id 登录失败, 请尝试重新登录",
                            )
                        }
                    }

                    else -> {
                        AccountOperationResult(AccountOperationStatus.NO_WRAPPER, "不支持的 Wrapper 类型")
                    }
                }
            } finally {
                loginStatus.update { false }
            }
        }

        fun logout(id: Long, platform: CometPlatform): AccountOperationResult {
            logger.info { "正在尝试注销账号 $id 于 ${platform.name} 平台" }

            return if (!hasAccount(id, platform)) {
                AccountOperationResult(AccountOperationStatus.NOT_FOUND, "注销账号失败: 找不到对应账号")
                    .also {
                        logger.info { it.message }
                    }
            } else {
                val result = transaction {
                    AccountDataTable.deleteWhere {
                        AccountDataTable.id eq id and
                            (AccountDataTable.platform eq platform)
                    }
                }

                if (result != 0) {
                    try {
                        cometInstances.find { it.id == id && it.platform == platform }?.close()
                        cometInstances.removeIf { it.id == id && it.platform == platform }

                        AccountOperationResult(AccountOperationStatus.OK, "注销账号成功")
                            .also {
                                logger.info { it.message }
                            }
                    } catch (e: Exception) {
                        AccountOperationResult(AccountOperationStatus.INTERNAL_ERROR, "发生异常")
                            .also {
                                logger.info { it.message }
                            }
                    }
                } else {
                    AccountOperationResult(AccountOperationStatus.NOT_FOUND, "注销账号失败: 找不到对应账号")
                        .also {
                            logger.info { it.message }
                        }
                }
            }
        }
    }
}
