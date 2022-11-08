package ren.natsuyuk1.comet.api.database

import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.platform.MiraiLoginProtocol

private val logger = KotlinLogging.logger {}

object AccountDataTable : IdTable<Long>("account_data") {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    val password: Column<String> = text("password")
    val platform = enumeration<LoginPlatform>("platform")
    val protocol = enumeration<MiraiLoginProtocol>("protocol").nullable().default(null)
}

class AccountData(id: EntityID<Long>) : Entity<Long>(id) {
    var password by AccountDataTable.password
    var platform by AccountDataTable.platform
    var protocol by AccountDataTable.protocol

    companion object : EntityClass<Long, AccountData>(AccountDataTable) {
        fun hasAccount(id: Long, platform: LoginPlatform): Boolean = transaction {
            !find {
                AccountDataTable.id eq id and (AccountDataTable.platform eq platform)
            }.empty()
        }

        fun registerAccount(
            id: Long,
            password: String,
            platform: LoginPlatform,
            protocol: MiraiLoginProtocol?
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

        fun getAccountData(id: Long, platform: LoginPlatform): AccountData? =
            transaction {
                find {
                    AccountDataTable.id eq id and (AccountDataTable.platform eq platform)
                }.firstOrNull()
            }
    }
}
