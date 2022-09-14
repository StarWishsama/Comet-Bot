package ren.natsuyuk1.comet.objects.apex

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ApexLegendDataTable : IdTable<UUID>("arcaea_user_data") {
    override val id: Column<EntityID<UUID>> = ApexLegendDataTable.uuid("user").entityId()
    override val primaryKey = PrimaryKey(id)

    val userID = varchar("user_id", 9)
}

class ApexLegendData(id: EntityID<UUID>) : UUIDEntity(id) {
    // 代表玩家的 Apex 唯一 ID
    var userID by ApexLegendDataTable.userID

    companion object : UUIDEntityClass<ApexLegendData>(ApexLegendDataTable) {
        fun isBound(uuid: UUID): Boolean = transaction {
            !find { ApexLegendDataTable.id eq uuid }.empty()
        }

        fun createData(uuid: UUID, userId: String) = transaction {
            ApexLegendDataTable.insert {
                it[id] = uuid
                it[userID] = userId
            }
        }

        fun updateID(uuid: UUID, userID: String) = transaction {
            (findById(uuid) ?: return@transaction).userID = userID
        }

        fun getUserApexData(uuid: UUID) = transaction {
            find { ApexLegendDataTable.id eq uuid }.firstOrNull()
        }
    }
}
