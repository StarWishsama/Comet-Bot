package ren.natsuyuk1.comet.objects.arcaea

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ArcaeaUserDataTable : IdTable<UUID>("arcaea_user_data") {
    override val id: Column<EntityID<UUID>> = ArcaeaUserDataTable.uuid("user").entityId()
    override val primaryKey = PrimaryKey(id)

    val userID = varchar("user_id", 9)
}

class ArcaeaUserData(id: EntityID<UUID>) : UUIDEntity(id) {
    // 代表玩家的 Arcaea 唯一 ID
    var userID by ArcaeaUserDataTable.userID

    companion object : UUIDEntityClass<ArcaeaUserData>(ArcaeaUserDataTable) {
        fun isBound(uuid: UUID): Boolean = transaction {
            !ArcaeaUserData.find { ArcaeaUserDataTable.id eq uuid }.empty()
        }

        fun createData(uuid: UUID, userId: String) = transaction {
            ArcaeaUserDataTable.insert {
                it[id] = uuid
                it[userID] = userId
            }
        }

        fun updateID(uuid: UUID, userID: String) = transaction {
            (ArcaeaUserData.findById(uuid) ?: return@transaction).userID = userID
        }

        fun getUserArcaeaData(uuid: UUID) = transaction {
            ArcaeaUserData.find { ArcaeaUserDataTable.id eq uuid }.firstOrNull()
        }
    }
}
