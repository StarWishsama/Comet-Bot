package ren.natsuyuk1.comet.cli.storage

import mu.KotlinLogging
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insertAndGetId

private val logger = KotlinLogging.logger {}

enum class LoginPlatform {
    QQ, TELEGRAM,
}

object AccountDataTable : IdTable<Long>() {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    val password: Column<String> = text("password")
    val platform = enumeration<LoginPlatform>("platform")
}

class AccountData(id: EntityID<Long>) : Entity<Long>(id) {
    val password by AccountDataTable.password
    val platform by AccountDataTable.platform

    companion object : EntityClass<Long, AccountData>(AccountDataTable) {
        fun hasAccount(id: Long, platform: LoginPlatform): Boolean = !find {
            AccountDataTable.id eq id
            AccountDataTable.platform eq platform
        }.empty()

        fun registerAccount(id: Long, password: String, platform: LoginPlatform): EntityID<Long> {
            logger.debug { "Creating comet bot account ($id) in $platform platform" }

            return AccountDataTable.insertAndGetId {
                it[AccountDataTable.id] = id
                it[AccountDataTable.password] = password
                it[AccountDataTable.platform] = platform
            }
        }
    }
}
